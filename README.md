# grpc-mock-server

[![codecov](https://codecov.io/gh/UDarya/grpc-mock-server/branch/main/graph/badge.svg?token=H2S3WS2G6J)](https://codecov.io/gh/UDarya/grpc-mock-server)


## Features
- Generate mock services for given proto API and run grpc service with these services
- Provide API to mock `request/response` for grpc service. Mock data is stored in the `hashMap` and available until restart of `mock-server` instance.
- Can be started in the separate container in the kubernetes for `dev` enviroment.

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

**Request example:**
```
{
    "serviceName": "TestFXAPI",
    "methodName": "getRates",
    "requestJSON": "{
                      "currencyFrom": "USD",
                      "currencyTo": "EUR"
                    }",
    "responseJSON": "{
                       "rate": 0.85
                     }"
}
```
