package com.benwanlh.peopleservice.grpc.mappers

import com.benwanlh.people.v1.person
import com.benwanlh.peopleservice.repositories.entities.Person
import com.benwanlh.people.v1.Person as PersonProto
import com.benwanlh.people.v1.Role as RoleProto

fun Person.toProto(): PersonProto = person {
    id = this@toProto.id!!
    name = this@toProto.name
    roles += this@toProto.roles.map { RoleProto.valueOf(it.name) }
    this@toProto.bio?.let { bio = it }
    this@toProto.birthDate?.let { birthDate = it.toString() }
    this@toProto.photoUrl?.let { photoUrl = it }
}