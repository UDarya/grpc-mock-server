package io.github.udarya.mockserver

import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import io.github.udarya.mockserver.util.exclude
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode


class CallSpy {
    private val calls: HashMap<String, MutableList<String>> = hashMapOf()
    private val jsonPrinter = JsonFormat.printer().includingDefaultValueFields()

    fun traceCall(methodName: String, request: MessageOrBuilder) {
        val requestJson = jsonPrinter.print(request)
        if (calls[methodName] == null) {
            calls[methodName] = mutableListOf(requestJson)
        } else {
            calls[methodName]?.add(requestJson)
        }
    }

    fun verify(methodName: String, requestJson: String, excludeFields: List<String>): Boolean {
        if (!calls.containsKey(methodName)) {
            throw AssertionError("There is not method [$methodName] in the trace list!")
        }
        val requests = calls[methodName]
        val requestsWithoutFields = requests!!.map { it.exclude(excludeFields) }
        val requestWithoutFields = requestJson.exclude(excludeFields)
        val traceListContainsRq = requestsWithoutFields.any {
            JSONCompare.compareJSON(it, requestWithoutFields, JSONCompareMode.LENIENT).passed()
        }
        if (traceListContainsRq) {
            return true
        } else {
            requestsWithoutFields.forEach { println("Method was called with request: $it") }
            throw AssertionError("There is not call with request: $requestWithoutFields in the trace list! ")
        }
    }

    fun resetMethodCalls(methodName: String) {
        if (!calls.containsKey(methodName)) return

        calls[methodName] = mutableListOf()
    }
}
