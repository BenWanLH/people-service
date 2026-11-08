package com.benwanlh.peopleservice.services

import com.benwanlh.movies.v1.MovieCreditsServiceGrpc
import com.benwanlh.movies.v1.deleteCreditsByPersonRequest
import com.benwanlh.peopleservice.dtos.PersonDetails
import com.benwanlh.peopleservice.exceptions.InvalidPhotoException
import com.benwanlh.peopleservice.exceptions.PersonNotFoundException
import com.benwanlh.peopleservice.exceptions.PhotoNotFoundException
import com.benwanlh.peopleservice.repositories.PersonRepository
import com.benwanlh.peopleservice.repositories.entities.Person
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

@Service
@Validated
@Transactional
class PersonService(
    private val personRepository: PersonRepository,
    private val movieCreditServiceStub:  MovieCreditsServiceGrpc.MovieCreditsServiceBlockingStub,
    @Value("\${photo.directory}") photoStoragePath: String,


    ) {
    private val photoDirectory: Path = Path.of(photoStoragePath)

    private val allowedImageTypes = mapOf(
        "image/jpeg" to "jpg",
        "image/png" to "png",
        "image/webp" to "webp",
    )

    fun getPersonById(id: Long): Person =
        personRepository.findByIdOrNull(id) ?: throw PersonNotFoundException(id)

    fun createPerson(@Valid details: PersonDetails): Person =
        personRepository.save(
            Person(
                name = details.name.trim(),
                birthDate = details.birthDate,
                bio = details.bio.blankToNull(),
                roles = details.roles.toMutableSet(),
            )
        )

    fun listAllPerson(): List<Person> {
        return personRepository.findAll()
    }

    fun getAllPersonsByIds(ids: Set<Long>): List<Person> {
        return personRepository.findAllById(ids)
    }

    fun searchPersonsByName(name: String): List<Person> {
        if (name.isBlank()) return emptyList()

        return personRepository.findPersonByNameContainingIgnoreCase(name.trim())
    }

    fun updatePerson(id: Long, @Valid person: PersonDetails): Person {
        val existing = getPersonById(id)

        return personRepository.save(Person(
            id = id,
            name = person.name.trim(),
            birthDate = person.birthDate,
            bio = person.bio.blankToNull(),
            photoFilename =  existing.photoFilename,
            roles = person.roles.toMutableSet(),
        ))
    }

    fun deletePerson(id: Long) {
        val person = getPersonById(id)

        personRepository.deleteById(id)

        movieCreditServiceStub.deleteCreditsByPerson(deleteCreditsByPersonRequest {this.personId = id})

        person.photoFilename?.let { Files.deleteIfExists(photoDirectory.resolve(it)) }
    }

    fun getPhoto(personId: Long): Resource {
        val filename = this.getPersonById(personId).photoFilename
            ?: throw PhotoNotFoundException(personId)

        val file = FileSystemResource(photoDirectory.resolve(filename))
        if (!file.exists()) throw PhotoNotFoundException(personId)

        return file
    }

    fun uploadPhoto(personId: Long, file: MultipartFile): Person {
        val person = this.getPersonById(personId)

        if (file.isEmpty) throw InvalidPhotoException("File is empty")
        val extension = allowedImageTypes[file.contentType]
            ?: throw InvalidPhotoException("Only JPEG, PNG or WebP images are allowed")

        Files.createDirectories(photoDirectory)
        val filename = "${UUID.randomUUID()}.$extension"
        val target = photoDirectory.resolve(filename)
        file.inputStream.use { Files.copy(it, target) }

        val oldFilename = person.photoFilename

        person.photoFilename = filename

        oldFilename?.let { Files.deleteIfExists(photoDirectory.resolve(it)) }

        return person
    }

    private fun String?.blankToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}