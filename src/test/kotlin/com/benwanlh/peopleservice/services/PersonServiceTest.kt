package com.benwanlh.peopleservice.services

import com.benwanlh.movies.v1.MovieCreditsServiceGrpc
import com.benwanlh.peopleservice.dtos.PersonDetails
import com.benwanlh.peopleservice.exceptions.PersonNotFoundException
import com.benwanlh.peopleservice.repositories.PersonRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class PersonServiceTest {

    private val personRepository = mockk<PersonRepository>()
    private val movieCreditServiceStub = mockk<MovieCreditsServiceGrpc.MovieCreditsServiceBlockingStub>(relaxed = true)
    private val personService = PersonService(personRepository, movieCreditServiceStub)

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
        every { personRepository.existsById(any()) } returns false

        assertThrows<PersonNotFoundException> {
            personService.deletePerson(99L)
        }
    }

    @Test
    fun `updatePerson throws PersonNotFoundException when id does not exist`() {
        every { personRepository.existsById(any()) } returns false

        assertThrows<PersonNotFoundException> {
            personService.updatePerson(99L, PersonDetails(name = "test person"))
        }
    }

    @Test
    fun `deletePerson removes the person and their credits in movie-service`() {
        every { personRepository.existsById(1L) } returns true
        every { personRepository.deleteById(1L) } just Runs

        personService.deletePerson(1L)

        verify { personRepository.deleteById(1L) }
        verify { movieCreditServiceStub.deleteCreditsByPerson(match { it.personId == 1L }) }
    }
}