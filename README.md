# grpc-mock-server

[![codecov](https://codecov.io/gh/UDarya/grpc-mock-server/branch/main/graph/badge.svg?token=H2S3WS2G6J)](https://codecov.io/gh/UDarya/grpc-mock-server)


## Features
 - Generate mock services for given proto API and run grpc service with these services.
 - Provide API to mock `request/response` for grpc service. 
 - Can be started in the separate container in the kubernetes for `dev` enviroment.
 - Provide API for `spy` functional (can check if method was called with certain response).
 - Provide API for reset mock data.

## Before running

Classes for grpc api should be generated in the project.
Example for configuration `com.google.protobuf` gradle plugin.
```
sourceSets {
    main {
        java {
            srcDirs(
                "${protobuf.protobuf.generatedFilesBaseDir}/main/java",
                "${protobuf.protobuf.generatedFilesBaseDir}/main/grpckt",
                "${protobuf.protobuf.generatedFilesBaseDir}/main/grpc"
            )
        }
        proto {
            srcDir(
                 "src/main/kotlin/github/udarya/mockserver/proto"
            )
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protocVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.28.1"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:0.1.1"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}
```

`grpc-mock-server` should be added to the project as gradle dependency:

```
implementation("github.udarya.mockserver:grpc-mock-server:$version")
```

## Running mocks
Generate and run mock for defined proto API.

Example of using mock generator:
```
@ExperimentalStdlibApi
fun main() {
    val service = generateAndRunGrpcMock(
        port,
        MockStructure("github.udarya.mockserver.TestFXAPIGrpcKt", "TestFXAPI")
    )
    service.awaitTermination()
}
```

## Mock data API
Mock `request/response` for grpc service.

```
rpc AddMockData(AddMockDataRequest) returns (AddMockDataResponse);
```

**Request:**
```
message AddMockDataRequest {
  string serviceName = 1; // service name from .proto file
  string methodName = 2;
  string requestJSON = 3;
  string responseJSON = 4;
}
```

**Response:**
```
message AddMockDataResponse {

}
```

**Example of use:**
```
mockServerAPIGrpc.addMockData(
    MockServerApiProto.AddMockDataRequest.newBuilder()
        .setServiceName("TestFXAPI")
        .setMethodName("getRates")
        .setRequestJson(jsonPrinter.print(getRatesRq))
        .setResponseJson(jsonPrinter.print(getRatesRs))
        .build()
)
```
**Important**: use `protobuf-java-util` to convert request to JSON and avoid additional proto fields (`JsonFormat.printer().includingDefaultValueFields()`) 
More examples can be founded in `GrpcMockServiceTest`.

## Verify method call API
```
rpc VerifyMethodCall(VerifyMethodCallRequest) returns (VerifyMethodCallResponse);
```

**Request:**
```
message VerifyMethodCallRequest {
  string method_name = 1;
  // Request with default fields. Use JsonFormat from protobuf-java-util library
  string request_json = 2;
  // Exclude these fields in comparing
  repeated string exclude_fields = 3;
}
```

**Response:**
```
message VerifyMethodCallResponse {
  bool is_success = 1;
  string error_message = 2;
}
```
**Example of use:**
```
mockServerAPIGrpc.verifyMethodCall(
    MockServerApiProto.VerifyMethodCallRequest.newBuilder()
        .setMethodName(testFXAPIGrpc::getRates.name)
        .setRequestJson(jsonPrinter.print(fxRequestWithDifferentCurrencyTo))
        .addAllExcludeFields(listOf("currencyFrom"))
        .build()
)
```
More examples can be founded in `SpyTest`.

## Reset mock data API
Delete mock data for method.
```
rpc ResetMethodCalls(ResetMethodCallsRequest) returns (ResetMethodCallsResponse);
```

**Request:**
```
message ResetMethodCallsRequest {
  string method_name = 1;
}
```

**Response:**
```
message ResetMethodCallsResponse {

}
```

**Example of use:**
```
mockServerAPIGrpc.resetMethodCalls(
    MockServerApiProto.ResetMethodCallsRequest.newBuilder()
        .setMethodName("getRates")
        .build()
)
```
More examples can be founded in `SpyTest`.

