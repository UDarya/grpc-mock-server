package io.github.udarya.mockserver

import com.google.protobuf.util.JsonFormat
import io.github.udarya.util.initChannel
import io.github.udarya.util.testFxRequest
import io.grpc.ManagedChannel
import io.grpc.Server
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe


@ExperimentalStdlibApi
class SpyTest : ShouldSpec({
    val port = 8005
    val host = "localhost"

    val jsonPrinter = JsonFormat.printer().includingDefaultValueFields()

    lateinit var mockServer: Server

    lateinit var mockServerAPIGrpc: MockServerAPIGrpc.MockServerAPIBlockingStub
    lateinit var testFXAPIGrpc: TestFXAPIGrpc.TestFXAPIBlockingStub

    beforeTest {
        val channel: ManagedChannel = initChannel(host, port)
        mockServer = generateAndRunGrpcMock(
            port,
            MockStructure("io.github.udarya.mockserver.TestFXAPIGrpcKt", "TestFXAPI")
        )
        testFXAPIGrpc = TestFXAPIGrpc.newBlockingStub(channel)
        mockServerAPIGrpc = MockServerAPIGrpc.newBlockingStub(channel)

    }

    afterTest {
        mockServer.shutdown()
    }

    should("return true if method was called with this request") {
        val fxRequest = testFxRequest()
        testFXAPIGrpc.getRates(fxRequest)

        val fxRequestWithDifferentCurrencyTo = testFxRequest(currencyFrom = "CAD")

        val verifyRs = mockServerAPIGrpc.verifyMethodCall(
            MockServerApiProto.VerifyMethodCallRequest.newBuilder()
                .setMethodName(testFXAPIGrpc::getRates.name)
                .setRequestJson(jsonPrinter.print(fxRequestWithDifferentCurrencyTo))
                .addAllExcludeFields(listOf("currencyFrom"))
                .build()
        )
        verifyRs shouldBe MockServerApiProto.VerifyMethodCallResponse.newBuilder().setIsSuccess(true).build()
    }

    should("return error if method was not called") {
        val verifyRs = mockServerAPIGrpc.verifyMethodCall(
            MockServerApiProto.VerifyMethodCallRequest.newBuilder()
                .setMethodName(testFXAPIGrpc::getRates.name)
                .setRequestJson(jsonPrinter.print(testFxRequest()))
                .addAllExcludeFields(listOf("currencyFrom"))
                .build()
        )

        verifyRs.isSuccess shouldBe false
        verifyRs.errorMessage.length shouldNotBe 0
    }

    should("reset method calls trace") {
        val fxRequest = testFxRequest()
        testFXAPIGrpc.getRates(fxRequest)

        mockServerAPIGrpc.resetMethodCalls(
            MockServerApiProto.ResetMethodCallsRequest.newBuilder()
                .setMethodName("getRates")
                .build()
        )

        val verifyRs = mockServerAPIGrpc.verifyMethodCall(
            MockServerApiProto.VerifyMethodCallRequest.newBuilder()
                .setMethodName(testFXAPIGrpc::getRates.name)
                .setRequestJson(jsonPrinter.print(testFxRequest()))
                .addAllExcludeFields(listOf("currencyFrom"))
                .build()
        )

        verifyRs.isSuccess shouldBe false
        verifyRs.errorMessage.length shouldNotBe 0
    }
})
