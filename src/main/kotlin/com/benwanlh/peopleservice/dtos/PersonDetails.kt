package com.benwanlh.peopleservice.dtos

import com.benwanlh.peopleservice.enums.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import java.time.LocalDate
import java.util.Collections.emptySet

data class PersonDetails(
    @field:NotBlank
    val name: String,

    @field:Past
    val birthDate: LocalDate? = null,

    val bio: String? = null,

    val photoUrl: String? = null,

    val roles: Set<Role> = emptySet(),
)
