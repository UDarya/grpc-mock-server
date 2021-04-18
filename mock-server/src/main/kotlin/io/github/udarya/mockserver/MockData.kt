package io.github.udarya.mockserver

data class MockData(
    /**
     * Map with mock data that where key=serviceName value is map
     * where key=methodName value is map
     * where key=request value=response
     * */
    val dataMap: MutableMap<String, MutableMap<String, MutableMap<String, String>>> = mutableMapOf()
)
