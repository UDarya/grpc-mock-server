package github.udarya.mockserver

import com.google.protobuf.util.JsonFormat
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaType

/**
 * Build classes for mock services.
 * */
@ExperimentalStdlibApi
internal fun buildMockScripts(mockStructure: MockStructure): MockScript {
    val grpcClass = Class.forName(mockStructure.kotlinGrpcClassName).kotlin
    val implementationClassName = "${grpcClass.simpleName}Impl"
    val coroutineImplBaseClassName =
        "${grpcClass.qualifiedName}.${mockStructure.serviceName}CoroutineImplBase"
    val coroutineImplBaseClass = grpcClass.java.classes.find { it.canonicalName == coroutineImplBaseClassName }
        ?: throw RuntimeException("There is not class with name: $coroutineImplBaseClassName")

    val declaredMethods = coroutineImplBaseClass.kotlin.declaredMemberFunctions
        .filter { !it.name.contains("bindService") }
    val typeSpec = TypeSpec.classBuilder(implementationClassName)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(
                    "serviceMockData",
                    MutableMap::class.java
                        .asClassName()
                        .parameterizedBy(
                            String::class.asTypeName(),
                            MutableMap::class.java.asClassName()
                                .parameterizedBy(String::class.asTypeName(), String::class.asTypeName())
                        )
                )
                .build()
        )
        .addProperty(
            PropertySpec.builder("jsonPrinter", JsonFormat.Printer::class)
                .addModifiers(KModifier.PRIVATE)
                .initializer(CodeBlock.of("JsonFormat.printer().includingDefaultValueFields()"))
                .build()
        )
        .addProperty(
            PropertySpec.builder("jsonParser", JsonFormat.Parser::class)
                .addModifiers(KModifier.PRIVATE)
                .initializer(CodeBlock.of("JsonFormat.parser()"))
                .build()
        )
        .addProperty(
            PropertySpec.builder(
                "serviceMockData",
                MutableMap::class.java
                    .asClassName()
                    .parameterizedBy(
                        String::class.asTypeName(),
                        MutableMap::class.java.asClassName()
                            .parameterizedBy(String::class.asTypeName(), String::class.asTypeName())
                    )
            )
                .addModifiers(KModifier.PRIVATE)
                .initializer(CodeBlock.of("serviceMockData"))
                .build()
        )
        .addFunctions(
            declaredMethods
                .map { method ->
                    val requestType = method.valueParameters
                        .filter { it.name != null }
                        .map { Pair(it.name, it.type) }
                        .first()
                    val responseType: KType = if (method.returnType.arguments.isNotEmpty()) {
                        method.returnType.arguments.first().type!!
                    } else {
                        method.returnType
                    }
                    val funcBuilder = FunSpec.builder(method.name).apply { addModifiers(KModifier.OVERRIDE) }
                        .returns(method.returnType.javaType)
                        .addParameter(
                            ParameterSpec.builder(requestType.first!!, requestType.second.javaType).build()
                        )
                    if (method.isSuspend) {
                        funcBuilder.addModifiers(KModifier.SUSPEND)
                    }
                    if (method.returnType != responseType) {
                        val flow = MemberName("kotlinx.coroutines.flow", "flow")
                        funcBuilder.addCode(
                            """
                                |return %M {
                                |println("${method.name} call")
                                |val builder = ${responseType.asTypeName()}.newBuilder()
                                |val rqStr = jsonPrinter.print(request)
                                |println("Request: " + rqStr)
                                |val mocksForMethod = serviceMockData["${method.name}"]
                                |if (mocksForMethod == null || mocksForMethod.isEmpty()) {
                                |println("There is not mock data for method [${method.name}]")
                                |builder.build()
                                |} else {
                                |val rsStr = mocksForMethod[rqStr]
                                |println("Response: " + rsStr)
                                |jsonParser.merge(rsStr, builder)
                                |builder.build()
                                |}
                                |}
                            |""".trimMargin(),
                            flow
                        )
                    } else {
                        funcBuilder.addCode(
                            """
                                |println("${method.name} call")
                                |val rqStr = jsonPrinter.print(request)
                                |val builder = ${responseType.asTypeName()}.newBuilder()
                                |println("Request: " + rqStr)
                                |val mocksForMethod = serviceMockData["${method.name}"]
                                |if (mocksForMethod == null || mocksForMethod.isEmpty()) {
                                |println("There is not mock data for method [${method.name}]")
                                |return builder.build()
                                |}
                                |val rsStr = mocksForMethod[rqStr]
                                |println("Response: " + rsStr)
                                |jsonParser.merge(rsStr, builder)
                                |return builder.build()
                            |""".trimMargin()
                        )
                    }

                    funcBuilder.build()
                }
        )
        .superclass(coroutineImplBaseClass)

    return MockScript(
        mockStructure.serviceName,
        FileSpec.builder(coroutineImplBaseClass.packageName, "${mockStructure.serviceName}Impl")
            .addType(typeSpec.build()).build()
            .toString(),
        implementationClassName
    )
}
