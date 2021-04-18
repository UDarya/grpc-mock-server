package io.github.udarya.util

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

fun initChannel(host: String, port: Int): ManagedChannel {
    return ManagedChannelBuilder.forAddress(host, port).also {
        if (port == 443) it.useTransportSecurity() else it.usePlaintext()
    }.build()
}
