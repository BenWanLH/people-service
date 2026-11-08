package com.benwanlh.peopleservice.controllers.rest

import com.benwanlh.peopleservice.exceptions.InvalidPhotoException
import com.benwanlh.peopleservice.exceptions.PersonNotFoundException
import com.benwanlh.peopleservice.exceptions.PhotoNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.multipart.MaxUploadSizeExceededException

@ControllerAdvice
class RestExceptionHandlers {
    @ExceptionHandler(PersonNotFoundException::class, PhotoNotFoundException::class)
    fun notFound(e: RuntimeException): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)

    @ExceptionHandler(InvalidPhotoException::class)
    fun badRequest(e: InvalidPhotoException): ResponseEntity<String> =
        ResponseEntity.badRequest().body(e.message)

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun uploadSizeExceed(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body("File must be 5MB or smaller")
}