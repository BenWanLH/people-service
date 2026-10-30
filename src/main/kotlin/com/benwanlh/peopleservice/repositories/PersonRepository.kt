package com.benwanlh.peopleservice.repositories

import com.benwanlh.peopleservice.repositories.entities.Person
import org.springframework.data.jpa.repository.JpaRepository

interface PersonRepository : JpaRepository<Person, Long> {
    fun findPersonByNameContainingIgnoreCase(name: String): List<Person>
}