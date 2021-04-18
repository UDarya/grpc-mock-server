package io.github.udarya.mockserver.intercept

import com.google.protobuf.MessageOrBuilder
import io.github.udarya.mockserver.CallSpy
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor


class CallSpyInterceptor(val callSpy: CallSpy) : ServerInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        return object : SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {
            override fun onMessage(request: ReqT) {
                val fullMethodName = call.methodDescriptor.fullMethodName
                callSpy.traceCall(fullMethodName.split("/").last().decapitalize(), request!! as MessageOrBuilder)
                super.onMessage(request)
            }
        }
    }
}
