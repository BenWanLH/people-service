package com.benwanlh.peopleservice.controllers

import com.benwanlh.peopleservice.exceptions.PersonNotFoundException
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.graphql.execution.ErrorType
import jakarta.validation.ConstraintViolationException

import java.time.format.DateTimeParseException

@ControllerAdvice
class GraphQlExceptionHandlers {

    @GraphQlExceptionHandler
    fun notFound(e: PersonNotFoundException, env: DataFetchingEnvironment): GraphQLError =
        GraphqlErrorBuilder.newError(env).errorType(ErrorType.NOT_FOUND).message(e.message).build()

    @GraphQlExceptionHandler(
        ConstraintViolationException::class,
        DateTimeParseException::class,
    )
    fun badRequest(e: Exception, env: DataFetchingEnvironment): GraphQLError =
        GraphqlErrorBuilder.newError(env).errorType(ErrorType.BAD_REQUEST).message(e.message).build()
}