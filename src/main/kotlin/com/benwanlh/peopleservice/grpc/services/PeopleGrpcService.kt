package com.benwanlh.peopleservice.grpc.services

import com.benwanlh.people.v1.BatchGetPeopleRequest
import com.benwanlh.people.v1.BatchGetPeopleResponse
import com.benwanlh.people.v1.CreatePersonRequest
import com.benwanlh.people.v1.DeletePersonRequest
import com.benwanlh.people.v1.DeletePersonResponse
import com.benwanlh.people.v1.GetPersonRequest
import com.benwanlh.people.v1.ListPeopleRequest
import com.benwanlh.people.v1.ListPeopleResponse
import com.benwanlh.people.v1.PeopleServiceGrpcKt
import com.benwanlh.people.v1.SearchPeopleRequest
import com.benwanlh.people.v1.SearchPeopleResponse
import com.benwanlh.people.v1.UpdatePersonRequest
import com.benwanlh.people.v1.batchGetPeopleResponse
import com.benwanlh.people.v1.deletePersonResponse
import com.benwanlh.people.v1.listPeopleResponse
import com.benwanlh.people.v1.searchPeopleResponse
import com.benwanlh.peopleservice.dtos.PersonDetails
import com.benwanlh.peopleservice.enums.Role
import com.benwanlh.people.v1.Person as PersonProto
import com.benwanlh.peopleservice.grpc.mappers.toProto
import com.benwanlh.peopleservice.grpc.wrappers.grpcCall
import com.benwanlh.peopleservice.services.PersonService
import org.springframework.stereotype.Service
import java.time.LocalDate


@Service
class PeopleGrpcService(
    private val personService: PersonService,
) : PeopleServiceGrpcKt.PeopleServiceCoroutineImplBase() {

    override suspend fun getPerson(request: GetPersonRequest): PersonProto = grpcCall {
        personService.getPersonById(request.id).toProto()
    }

    override suspend fun createPerson(request: CreatePersonRequest): PersonProto= grpcCall {
        val personDetails = PersonDetails(
            name = request.name,
            birthDate =if (request.hasBirthDate()) LocalDate.parse(request.birthDate) else null,
            bio = request.bio,
            photoUrl = request.photoUrl,
            roles = request.rolesList.map { Role.valueOf(it.name) }.toSet())

        return personService.createPerson(personDetails).toProto()
    }

    override suspend fun listPeople(request: ListPeopleRequest): ListPeopleResponse = grpcCall {
        val persons = personService.listAllPerson()

        return listPeopleResponse { people += persons.map { it.toProto() } }
    }

    override suspend fun batchGetPeople(request: BatchGetPeopleRequest): BatchGetPeopleResponse = grpcCall {
        val persons =  personService.getAllPersonsByIds(request.idsList.toSet())

        return batchGetPeopleResponse { people += persons.map { it.toProto() } }
    }

    override suspend fun searchPeople(request: SearchPeopleRequest): SearchPeopleResponse = grpcCall {
        val persons = personService.searchPersonsByName(request.name)

        return searchPeopleResponse { people += persons.map { it.toProto() } }
    }

    override suspend fun deletePerson(request: DeletePersonRequest): DeletePersonResponse = grpcCall {
        personService.deletePerson(request.id)

        return deletePersonResponse { }
    }

    override suspend fun updatePerson(request: UpdatePersonRequest): PersonProto = grpcCall {
        val personDetails = PersonDetails(
            name = request.name,
            bio = request.bio.trim(),
            photoUrl = request.photoUrl.trim(),
            birthDate = if (!request.birthDate.isEmpty()) LocalDate.parse(request.birthDate) else null,
            roles = request.rolesList.map { Role.valueOf(it.name) }.toMutableSet())

        return personService.updatePerson(request.id, personDetails).toProto()
    }
}