package github.udarya.mockserver

import com.google.protobuf.util.JsonFormat
import github.udarya.mockserver.util.exclude
import github.udarya.util.testFxRequest
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class JsonUtilTest : ShouldSpec({
    val jsonPrinter = JsonFormat.printer().includingDefaultValueFields()

    should("compare json without excluded fields") {
        val fxRequest = jsonPrinter.print(testFxRequest())
        val fxRequestWithDifferentCurrencyTo = jsonPrinter.print(testFxRequest(currencyTo = "USD"))
        val excludeFields = listOf("currencyTo")

        fxRequest.exclude(excludeFields) shouldBe fxRequestWithDifferentCurrencyTo.exclude(excludeFields)
    }
})
