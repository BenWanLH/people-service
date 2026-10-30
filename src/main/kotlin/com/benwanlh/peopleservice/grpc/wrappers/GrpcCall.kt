package com.benwanlh.peopleservice.grpc.wrappers


import com.benwanlh.peopleservice.exceptions.PersonNotFoundException
import io.grpc.Status
import io.grpc.StatusException
import jakarta.validation.ConstraintViolationException
import java.time.format.DateTimeParseException

inline fun <T> grpcCall(block: () -> T): T =
    try {
        block()
    } catch (e: PersonNotFoundException) {
        throw StatusException(Status.NOT_FOUND.withDescription(e.message))
    } catch (e: ConstraintViolationException) {
        throw StatusException(Status.INVALID_ARGUMENT.withDescription( e.constraintViolations.joinToString { "${it.propertyPath}: ${it.message}" }))
    } catch(e: DateTimeParseException) {
        throw StatusException(Status.INVALID_ARGUMENT.withDescription("birthDate must be in YYYY-MM-DD format"))
    }