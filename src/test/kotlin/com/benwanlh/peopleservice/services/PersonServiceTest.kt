package com.benwanlh.peopleservice.services

import com.benwanlh.peopleservice.dtos.PersonDetails
import com.benwanlh.peopleservice.exceptions.PersonNotFoundException
import com.benwanlh.peopleservice.repositories.PersonRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class PersonServiceTest {

    private val personRepository = mockk<PersonRepository>()
    private val personService = PersonService(personRepository)

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
}