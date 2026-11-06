package com.benwanlh.peopleservice.controllers

import com.benwanlh.movies.v1.MovieCreditsServiceGrpc
import com.benwanlh.peopleservice.dtos.PersonDetailInput
import com.benwanlh.peopleservice.enums.Role
import com.benwanlh.peopleservice.exceptions.PersonNotFoundException
import com.benwanlh.peopleservice.repositories.entities.Person
import com.benwanlh.peopleservice.services.PersonService
import org.springframework.graphql.data.federation.EntityMapping
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class PersonController(
    private val personService: PersonService,
) {

    @EntityMapping("Person")
    fun personByIds(@Argument idList: List<Long>): List<Person?> {
        val peopleById = personService.getAllPersonsByIds(idList.toSet()).associateBy { it.id }
        return idList.map { peopleById[it] }
    }

    @QueryMapping
    fun person(@Argument id: Long): Person? =
        try { personService.getPersonById(id) } catch (e: PersonNotFoundException) { null }

    @QueryMapping
    fun people(@Argument roles: List<Role>?, @Argument name: String?): List<Person> {
        val people = if (name.isNullOrBlank()) personService.listAllPerson()
        else personService.searchPersonsByName(name)
        if (roles.isNullOrEmpty()) return people
        return people.filter { person -> person.roles.any { it in roles } }
    }

    @MutationMapping
    fun createPerson(@Argument personDetail: PersonDetailInput): Person =
        personService.createPerson(personDetail.toPersonDetails())

    @MutationMapping
    fun updatePerson(@Argument id: Long, @Argument personDetail: PersonDetailInput): Person =
        personService.updatePerson(id, personDetail.toPersonDetails())

    @MutationMapping
     fun deletePerson(@Argument id: Long): Boolean {
        personService.deletePerson(id)
        return true
    }
}