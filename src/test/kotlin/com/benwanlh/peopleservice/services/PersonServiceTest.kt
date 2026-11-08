package com.benwanlh.peopleservice.services

import com.benwanlh.movies.v1.MovieCreditsServiceGrpc
import com.benwanlh.peopleservice.dtos.PersonDetails
import com.benwanlh.peopleservice.exceptions.InvalidPhotoException
import com.benwanlh.peopleservice.exceptions.PersonNotFoundException
import com.benwanlh.peopleservice.repositories.PersonRepository
import com.benwanlh.peopleservice.repositories.entities.Person
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional

class PersonServiceTest {

    @TempDir
    lateinit var tempDir: Path

    private val personRepository = mockk<PersonRepository>()
    private val movieCreditServiceStub = mockk<MovieCreditsServiceGrpc.MovieCreditsServiceBlockingStub>(relaxed = true)
    private val personService by lazy { PersonService(personRepository, movieCreditServiceStub, tempDir.toString()) }
    
    @Test
    fun `getPersonById throws PersonNotFoundException when id does not exist`() {
        every { personRepository.findById(99L) } returns Optional.empty()

        assertThrows<PersonNotFoundException> {
            personService.getPersonById(99L)
        }
    }

    @Test
    fun `createPerson trims name and set bio to null if blank  during creation`() {
        every { personRepository.save(any()) } answers { firstArg() }

        val person = personService.createPerson(
            PersonDetails(
                name = "test name with space at the end ",
                bio = ""))

        assertEquals("test name with space at the end", person.name)
        assertEquals(null, person.bio)
    }

    @Test
    fun `searchPersonsByName should return empty if name is blank`() {
        val persons = personService.searchPersonsByName("")

        assertEquals(0, persons.size)
    }

    @Test
    fun `deletePerson throws PersonNotFoundException when id does not exist`() {
        every { personRepository.findById(99L) } returns Optional.empty()

        assertThrows<PersonNotFoundException> {
            personService.deletePerson(99L)
        }
    }

    @Test
    fun `updatePerson throws PersonNotFoundException when id does not exist`() {
        every { personRepository.findById(99L) } returns Optional.empty()

        assertThrows<PersonNotFoundException> {
            personService.updatePerson(99L, PersonDetails(name = "test person"))
        }
    }

    @Test
    fun `deletePerson removes the person and their credits in movie-service`() {
        every { personRepository.findById(1L) } returns Optional.of(Person(id = 1L, name = "test person"))
        every { personRepository.deleteById(1L) } just Runs

        personService.deletePerson(1L)

        verify { personRepository.deleteById(1L) }
        verify { movieCreditServiceStub.deleteCreditsByPerson(match { it.personId == 1L }) }
    }

    @Test
    fun `uploadPhoto throws InvalidPhotoException with a non-image file`() {
        every { personRepository.findById(1L) } returns Optional.of(Person(id = 1L, name = "Keanu Reeves"))
        val file = MockMultipartFile("file", "notes.txt", "text/plain", "hello".toByteArray())

        assertThrows<InvalidPhotoException> { personService.uploadPhoto(1L, file) }
    }

    @Test
    fun `uploadPhoto throws InvalidPhotoException with a empty file`() {
        every { personRepository.findById(1L) } returns Optional.of(Person(id = 1L, name = "Keanu Reeves"))
        val file = MockMultipartFile("file", "notes.txt", "image/jpeg", ByteArray(0))

        assertThrows<InvalidPhotoException> { personService.uploadPhoto(1L, file) }
    }

    @Test
    fun `uploadPhoto throws PersonNotFoundException if personId is not found`() {
        every { personRepository.findById(any()) } returns Optional.empty()
        val file = MockMultipartFile("file", "notes.jpg", "image/jpeg", "hello".toByteArray())

        assertThrows<PersonNotFoundException> { personService.uploadPhoto(1L, file) }
    }

    @Test
    fun `uploadPhoto should update photoFilename of Person`() {
        every { personRepository.findById(1L) } returns Optional.of(Person(id = 1L, name = "Keanu Reeves"))

        val file = MockMultipartFile("file", "notes.jpg", "image/jpeg", "hello".toByteArray())
        val person = personService.uploadPhoto(1, file)

        val saved = tempDir.resolve(person.photoFilename!!)

        assertTrue(Files.exists(saved))
        assertTrue(person.photoFilename!!.endsWith(".jpg"))
    }

    @Test
    fun `uploadPhoto deletes the previous photo`() {
        val old = Files.createFile(tempDir.resolve("old.jpg"))
        every { personRepository.findById(1L) } returns Optional.of(Person(id = 1L, name = "Keanu Reeves", photoFilename = "old.jpg"))

        val person = personService.uploadPhoto(1L, MockMultipartFile("file", "new.png", "image/png", "hello".toByteArray()))

        assertFalse(Files.exists(old))
        assertTrue(Files.exists(tempDir.resolve(person.photoFilename!!)))
    }

    @Test
    fun `deletePerson deletes the photo file`() {
        val photo = Files.createFile(tempDir.resolve("photo.jpg"))
        every { personRepository.findById(1L) } returns Optional.of(Person(id = 1L, name = "Keanu Reeves", photoFilename = "photo.jpg"))
        every { personRepository.deleteById(1L) } just Runs

        personService.deletePerson(1L)

        assertFalse(Files.exists(photo))
    }
}