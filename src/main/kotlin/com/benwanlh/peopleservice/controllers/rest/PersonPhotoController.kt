package com.benwanlh.peopleservice.controllers.rest

import com.benwanlh.peopleservice.services.PersonService
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class PersonPhotoController(
    private val personService: PersonService,
) {
    @PostMapping("people/{id}/photo")
    fun uploadPhoto(@PathVariable id: Long, @RequestParam("file") file: MultipartFile): ResponseEntity<Void> {
        personService.uploadPhoto(id, file)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("people/{id}/photo")
    fun getPhoto(@PathVariable id: Long): ResponseEntity<Resource> {
        val photo = personService.getPhoto(id)
        val contentType = MediaTypeFactory.getMediaType(photo).orElse(MediaType.APPLICATION_OCTET_STREAM)

        return ResponseEntity.ok()
            .contentType(contentType).body(photo)
    }
}