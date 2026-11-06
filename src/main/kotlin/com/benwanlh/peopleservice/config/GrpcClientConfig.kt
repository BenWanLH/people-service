package com.benwanlh.peopleservice.config

import com.benwanlh.movies.v1.MovieCreditsServiceGrpc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.grpc.client.GrpcChannelFactory

@Configuration
class GrpcClientConfig {

    @Bean
    fun movieCreditsStub(channels: GrpcChannelFactory): MovieCreditsServiceGrpc.MovieCreditsServiceBlockingStub =
        MovieCreditsServiceGrpc.newBlockingStub(channels.createChannel("movies"))
}
