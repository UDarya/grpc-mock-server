package github.udarya.mockserver

import github.udarya.mockserver.intercept.CallSpyInterceptor
import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import javax.script.ScriptEngineManager

/**
 * Generate mock class for given proto api and run grpc server
 * Kotlin classes for grpc api should be already generated in the project
 * @param mockStructures mock descriptions
 * @param port grpc server port
 * */
@ExperimentalStdlibApi
fun generateAndRunGrpcMock(port: Int, vararg mockStructures: MockStructure): Server {
    val mockScripts = mockStructures
        .map { mockStructure -> buildMockScripts(mockStructure) }

    val mockData = MockData()
    val mockInstances = createGrpcServerForMockInstance(mockScripts, mockData)

    val mockService = createGrpcServer(mockInstances, mockData, port)
    mockService.start()
    println("Grpc service started with port: $port")
    return mockService
}

internal fun createGrpcServerForMockInstance(
    mockScripts: List<MockScript>,
    mockData: MockData
): List<BindableService> {
    return mockScripts.map { mockScript ->
        val (classLoader, className) = ScriptEngineManager().getEngineByExtension("kts").eval(
            mockScript.script +
                "Pair(" +
                "${mockScript.implementedClassName}::class.java.classLoader, " +
                "${mockScript.implementedClassName}::class.java.name" +
                ")"
        ) as Pair<ClassLoader, String>
        val serviceMockData = mockData.dataMap[mockScript.serviceName]
        if (serviceMockData == null) {
            mockData.dataMap[mockScript.serviceName] = mutableMapOf()
        }
        classLoader.loadClass(className)
            .getDeclaredConstructor(Map::class.java)
            .newInstance(mockData.dataMap[mockScript.serviceName]) as BindableService
    }
}

private fun createGrpcServer(services: List<BindableService>, mockData: MockData, port: Int): Server {
    val callSpy = CallSpy()
    val builder = ServerBuilder.forPort(port)
    services.forEach { service ->
        builder.addService(service).intercept(CallSpyInterceptor(callSpy))
    }
    builder.addService(MockServerAPIImpl(mockData, callSpy))
    return builder.build()
}
