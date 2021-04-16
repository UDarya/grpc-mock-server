package github.udarya.util

import github.udarya.mockserver.TestFxApiProto

fun testFxRequest(
    currencyFrom: String = "USD",
    currencyTo: String = "EUR"
): TestFxApiProto.GetRatesRequest {
    return TestFxApiProto.GetRatesRequest.newBuilder()
        .setCurrencyFrom(currencyFrom)
        .setCurrencyTo(currencyTo)
        .build()
}
