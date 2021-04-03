package github.udarya.mockserver

import com.google.protobuf.util.JsonFormat
import github.udarya.util.initChannel
import io.grpc.ManagedChannel
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking

@ExperimentalStdlibApi
class GrpcMockServiceTest : ShouldSpec({
    val port = 8005
    val host = "localhost"

    val jsonPrinter = JsonFormat.printer().includingDefaultValueFields()

    should("create mock for fx test service and mock get rate response") {
        val server = generateAndRunGrpcMock(
            port,
            MockStructure("github.udarya.mockserver.TestFXAPIGrpcKt", "TestFXAPI")
        )
        val channel: ManagedChannel = runBlocking {
            initChannel(host, port)
        }
        val testFXAPIGrpc = TestFXAPIGrpc.newBlockingStub(channel)
        val mockServerAPIGrpc = MockServerAPIGrpc.newBlockingStub(channel)

        val getRatesRq = TestFxApiProto.GetRatesRequest.newBuilder()
            .setCurrencyFrom("USD")
            .setCurrencyTo("EUR")
            .build()
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

        server.shutdown()
    }
})
