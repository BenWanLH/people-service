package com.benwanlh.peopleservice.dtos

import com.benwanlh.peopleservice.enums.Role
import java.time.LocalDate

data class PersonDetailInput(
    val name: String,
    val roles: List<Role>,
    val birthDate: String? = null,
    val bio: String? = null,
) {
    fun toPersonDetails() = PersonDetails(
        name = name,
        birthDate = birthDate?.takeIf { it.isNotBlank() }?.let(LocalDate::parse),
        bio = bio,
        roles = roles.toSet(),
    )
}
