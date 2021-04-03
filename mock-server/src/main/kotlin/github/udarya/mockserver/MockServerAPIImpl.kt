package github.udarya.mockserver


/**
 * Provide GRPC service to mock data
 * */
class MockServerAPIImpl(
    private val mockData: MockData
) : MockServerAPIGrpcKt.MockServerAPICoroutineImplBase() {
    override suspend fun addMockData(request: MockServerApiProto.AddMockDataRequest): MockServerApiProto.AddMockDataResponse {
        var serviceMocks = mockData.dataMap[request.serviceName]
        if (serviceMocks == null) {
            mockData.dataMap[request.serviceName] = mutableMapOf()
            serviceMocks = mockData.dataMap[request.serviceName]
            if (serviceMocks!![request.methodName] == null) {
                serviceMocks[request.methodName] = mutableMapOf(request.requestJson to request.responseJson)
            } else {
                serviceMocks[request.methodName]!![request.requestJson] = request.responseJson
            }
        } else {
            val methodMocks = serviceMocks[request.methodName]
            if (methodMocks == null) {
                serviceMocks[request.methodName] = mutableMapOf(request.requestJson to request.responseJson)
            } else {
                methodMocks[request.methodName] = request.responseJson
            }
        }
        return MockServerApiProto.AddMockDataResponse.newBuilder().build()
    }
}

