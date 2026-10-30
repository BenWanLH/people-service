package com.benwanlh.peopleservice.services

import com.benwanlh.peopleservice.dtos.PersonDetails
import com.benwanlh.peopleservice.exceptions.PersonNotFoundException
import com.benwanlh.peopleservice.repositories.PersonRepository
import com.benwanlh.peopleservice.repositories.entities.Person
import jakarta.validation.Valid
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
@Transactional
class PersonService(
    private val personRepository: PersonRepository,
) {

    fun getPersonById(id: Long): Person =
        personRepository.findByIdOrNull(id) ?: throw PersonNotFoundException(id)

    fun createPerson(@Valid details: PersonDetails): Person =
        personRepository.save(
            Person(
                name = details.name.trim(),
                birthDate = details.birthDate,
                bio = details.bio.blankToNull(),
                photoUrl = details.photoUrl.blankToNull(),
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
        if (!personRepository.existsById(id)) throw PersonNotFoundException(id)

        return personRepository.save(Person(
            id = id,
            name = person.name.trim(),
            birthDate = person.birthDate,
            bio = person.bio.blankToNull(),
            photoUrl = person.photoUrl.blankToNull(),
            roles = person.roles.toMutableSet(),
        ))
    }

    fun deletePerson(id: Long) {
        if (!personRepository.existsById(id)) throw PersonNotFoundException(id)

        personRepository.deleteById(id)
    }

    private fun String?.blankToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}