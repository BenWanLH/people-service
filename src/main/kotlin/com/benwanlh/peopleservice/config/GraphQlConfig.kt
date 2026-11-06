package com.benwanlh.peopleservice.config

import org.springframework.boot.graphql.autoconfigure.GraphQlSourceBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.data.federation.FederationSchemaFactory

@Configuration
class GraphQlConfig {

    @Bean
    fun federationSchemaFactory() = FederationSchemaFactory()

    @Bean
    fun federationCustomizer(factory: FederationSchemaFactory) =
        GraphQlSourceBuilderCustomizer { it.schemaFactory(factory::createGraphQLSchema) }
}