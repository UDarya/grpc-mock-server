package github.udarya.mockserver

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldNotBe

@ExperimentalStdlibApi
class MockBuilderTest : ShouldSpec({
    should("Build class and create bindable service") {
        val mockStructure = MockStructure(
            "github.udarya.mockserver.MockServerAPIGrpcKt",
            "MockServerAPI"
        )
        val mockScript = buildMockScripts(mockStructure)
        val bindableService = createGrpcServerForMockInstance(listOf(mockScript), MockData())

        bindableService shouldNotBe null
    }
})
