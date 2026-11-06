package com.benwanlh.peopleservice.controllers

import com.benwanlh.movies.v1.MovieCreditsServiceGrpc
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.ParameterizedTypeReference
import org.springframework.graphql.execution.ErrorType
import org.springframework.graphql.test.tester.GraphQlTester
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureGraphQlTester
class PersonControllerTest @Autowired constructor(
    private val graphQlTester: GraphQlTester,
    private val movieCreditsStub: MovieCreditsServiceGrpc.MovieCreditsServiceBlockingStub,
) {


    @TestConfiguration
    class Mocks {
        @Bean
        @Primary
        fun mockMovieCreditsStub() = mockk<MovieCreditsServiceGrpc.MovieCreditsServiceBlockingStub>(relaxed = true)
    }

    private fun uniqueName(name: String) = "$name ${UUID.randomUUID()}"

    private fun createPerson(
        name: String,
        roles: List<String> = listOf("ACTOR"),
        birthDate: String? = null,
    ): String =
        graphQlTester.document(
            """
            mutation(${'$'}detail: PersonDetail!) {
              createPerson(personDetail: ${'$'}detail) { id }
            }
            """
        )
            .variable("detail", mapOf("name" to name, "roles" to roles, "birthDate" to birthDate))
            .execute()
            .path("createPerson.id").entity(String::class.java).get()

    @Test
    fun `_entities returns people in request order with null for unknown ids`() {
        val name = uniqueName("Keanu Reeves")
        val id = createPerson(name)

        graphQlTester.document(
            """
            query(${'$'}representations: [_Any!]!) {
              _entities(representations: ${'$'}representations) { ... on Person { id name } }
            }
            """
        )
            .variable(
                "representations", listOf(
                    mapOf("__typename" to "Person", "id" to "999999"),
                    mapOf("__typename" to "Person", "id" to id),
                )
            )
            .execute()
            .path("_entities[0]").valueIsNull()
            .path("_entities[1].id").entity(String::class.java).isEqualTo(id)
            .path("_entities[1].name").entity(String::class.java).isEqualTo(name)
    }

    @Test
    fun `person returns null for an unknown id`() {
        graphQlTester.document("{ person(id: 999999) { id } }")
            .execute()
            .path("person").valueIsNull()
    }

    @Test
    fun `people searches by name case-insensitively`() {
        val name = uniqueName("Carrie-Anne Moss")
        createPerson(name)

        graphQlTester.document("""{ people(name: "${name.uppercase()}") { name } }""")
            .execute()
            .path("people[*].name").entityList(String::class.java).containsExactly(name)
    }

    @Test
    fun `people filters by role`() {
        val director = uniqueName("Lana Wachowski")
        val actor = uniqueName("Laurence Fishburne")
        createPerson(director, roles = listOf("DIRECTOR", "WRITER"))
        createPerson(actor, roles = listOf("ACTOR"))

        val response = graphQlTester.document("{ people(roles: [DIRECTOR]) { name roles } }").execute()

        val names = response.path("people[*].name").entityList(String::class.java).get()
        val rolesPerPerson = response.path("people[*].roles")
            .entityList(object : ParameterizedTypeReference<List<String>>() {}).get()

        assertTrue(director in names)
        assertFalse(actor in names)
        assertTrue(rolesPerPerson.all { "DIRECTOR" in it })
    }

    @Test
    fun `people with a blank name lists everyone instead of searching`() {
        val name = uniqueName("Hugo Weaving")
        createPerson(name)

        graphQlTester.document("""{ people(name: "  ") { name } }""")
            .execute()
            .path("people[*].name").entityList(String::class.java).contains(name)
    }

    @Test
    fun `createPerson returns birthDate as YYYY-MM-DD and blank optional fields as null`() {
        graphQlTester.document(
            """
            mutation {
              createPerson(personDetail: {
                name: "  Joe Pantoliano  ", roles: [ACTOR], birthDate: "1951-09-12", bio: "  ", photoUrl: ""
              }) { name birthDate bio photoUrl }
            }
            """
        )
            .execute()
            .path("createPerson.name").entity(String::class.java).isEqualTo("Joe Pantoliano")
            .path("createPerson.birthDate").entity(String::class.java).isEqualTo("1951-09-12")
            .path("createPerson.bio").valueIsNull()
            .path("createPerson.photoUrl").valueIsNull()
    }

    @Test
    fun `createPerson with a blank name is a BAD_REQUEST`() {
        graphQlTester.document("""mutation { createPerson(personDetail: { name: " ", roles: [ACTOR] }) { id } }""")
            .execute()
            .errors().satisfy { errors ->
                assertEquals(1, errors.size)
                assertEquals(ErrorType.BAD_REQUEST, errors[0].errorType)
            }
    }

    @Test
    fun `createPerson with a malformed birthDate is a BAD_REQUEST`() {
        graphQlTester.document(
            """mutation { createPerson(personDetail: { name: "Gloria Foster", roles: [ACTOR], birthDate: "abc" }) { id } }"""
        )
            .execute()
            .errors().satisfy { errors ->
                assertEquals(1, errors.size)
                assertEquals(ErrorType.BAD_REQUEST, errors[0].errorType)
            }
    }

    @Test
    fun `updatePerson with an unknown id is NOT_FOUND`() {
        graphQlTester.document(
            """mutation { updatePerson(id: 999999, personDetail: { name: "Nobody", roles: [ACTOR] }) { id } }"""
        )
            .execute()
            .errors().satisfy { errors ->
                assertEquals(1, errors.size)
                assertEquals(ErrorType.NOT_FOUND, errors[0].errorType)
            }
    }

    @Test
    fun `deletePerson removes the person and asks movie-service to delete their credits`() {
        val id = createPerson(uniqueName("Marcus Chong"))

        graphQlTester.document("mutation { deletePerson(id: $id) }")
            .execute()
            .path("deletePerson").entity(Boolean::class.java).isEqualTo(true)

        verify { movieCreditsStub.deleteCreditsByPerson(match { it.personId == id.toLong() }) }

        graphQlTester.document("{ person(id: $id) { id } }")
            .execute()
            .path("person").valueIsNull()
    }
}
