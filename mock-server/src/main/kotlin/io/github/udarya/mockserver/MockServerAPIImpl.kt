package io.github.udarya.mockserver


/**
 * Provide GRPC service to mock data
 * */
class MockServerAPIImpl(
    private val mockData: MockData,
    private val callSpy: CallSpy
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
                methodMocks[request.requestJson] = request.responseJson
            }
        }
        return MockServerApiProto.AddMockDataResponse.newBuilder().build()
    }

    override suspend fun verifyMethodCall(request: MockServerApiProto.VerifyMethodCallRequest): MockServerApiProto.VerifyMethodCallResponse {
        return try {
            MockServerApiProto.VerifyMethodCallResponse.newBuilder()
                .setIsSuccess(callSpy.verify(request.methodName, request.requestJson, request.excludeFieldsList))
                .build()
        } catch (e: AssertionError) {
            MockServerApiProto.VerifyMethodCallResponse.newBuilder()
                .setIsSuccess(false)
                .setErrorMessage(e.message)
                .build()
        }
    }

    override suspend fun resetMethodCalls(request: MockServerApiProto.ResetMethodCallsRequest): MockServerApiProto.ResetMethodCallsResponse {
        callSpy.resetMethodCalls(request.methodName)
        return MockServerApiProto.ResetMethodCallsResponse.newBuilder().build()
    }
}

