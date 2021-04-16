package github.udarya.mockserver

import com.google.protobuf.util.JsonFormat
import github.udarya.util.initChannel
import github.udarya.util.testFxRequest
import io.grpc.ManagedChannel
import io.grpc.Server
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@ExperimentalStdlibApi
class GrpcMockServiceTest : ShouldSpec({
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
            MockStructure("github.udarya.mockserver.TestFXAPIGrpcKt", "TestFXAPI")
        )
        testFXAPIGrpc = TestFXAPIGrpc.newBlockingStub(channel)
        mockServerAPIGrpc = MockServerAPIGrpc.newBlockingStub(channel)

    }

    afterTest {
        mockServer.shutdown()
    }

    should("create mock for fx test service and mock get rate response") {
        val getRatesRq = testFxRequest()
        val getRatesRqJson = jsonPrinter.print(getRatesRq)
        val getRatesRs = TestFxApiProto.GetRatesResponse.newBuilder().setRate(0.85f).build()

        mockServerAPIGrpc.addMockData(
            MockServerApiProto.AddMockDataRequest.newBuilder()
                .setServiceName("TestFXAPI")
                .setMethodName("getRates")
                .setRequestJson(getRatesRqJson)
                .setResponseJson(jsonPrinter.print(getRatesRs))
                .build()
        )

        testFXAPIGrpc.getRates(getRatesRq) shouldBe getRatesRs
    }

    should("Overwrite response for the same request") {
        val getRatesRq = testFxRequest()
        val getRatesRs = TestFxApiProto.GetRatesResponse.newBuilder().setRate(0.85f).build()

        mockServerAPIGrpc.addMockData(
            MockServerApiProto.AddMockDataRequest.newBuilder()
                .setServiceName("TestFXAPI")
                .setMethodName("getRates")
                .setRequestJson(jsonPrinter.print(getRatesRq))
                .setResponseJson(jsonPrinter.print(getRatesRs))
                .build()
        )

        testFXAPIGrpc.getRates(getRatesRq) shouldBe getRatesRs

        val overwriteGetRatesRs = TestFxApiProto.GetRatesResponse.newBuilder().setRate(0.253f).build()
        mockServerAPIGrpc.addMockData(
            MockServerApiProto.AddMockDataRequest.newBuilder()
                .setServiceName("TestFXAPI")
                .setMethodName("getRates")
                .setRequestJson(jsonPrinter.print(getRatesRq))
                .setResponseJson(jsonPrinter.print(overwriteGetRatesRs))
                .build()
        )

        testFXAPIGrpc.getRates(getRatesRq) shouldBe overwriteGetRatesRs
    }

    should("Mock different pairs request/response") {
        val getRatesRq1 = testFxRequest()
        val getRatesRs1 = TestFxApiProto.GetRatesResponse.newBuilder().setRate(0.85f).build()

        mockServerAPIGrpc.addMockData(
            MockServerApiProto.AddMockDataRequest.newBuilder()
                .setServiceName("TestFXAPI")
                .setMethodName("getRates")
                .setRequestJson(jsonPrinter.print(getRatesRq1))
                .setResponseJson(jsonPrinter.print(getRatesRs1))
                .build()
        )

        testFXAPIGrpc.getRates(getRatesRq1) shouldBe getRatesRs1

        val getRatesRq2 = testFxRequest(currencyTo = "RUB")
        val getRatesRs2 = TestFxApiProto.GetRatesResponse.newBuilder().setRate(0.55f).build()

        mockServerAPIGrpc.addMockData(
            MockServerApiProto.AddMockDataRequest.newBuilder()
                .setServiceName("TestFXAPI")
                .setMethodName("getRates")
                .setRequestJson(jsonPrinter.print(getRatesRq2))
                .setResponseJson(jsonPrinter.print(getRatesRs2))
                .build()
        )
        testFXAPIGrpc.getRates(getRatesRq1) shouldBe getRatesRs1
        testFXAPIGrpc.getRates(getRatesRq2) shouldBe getRatesRs2
    }
})
