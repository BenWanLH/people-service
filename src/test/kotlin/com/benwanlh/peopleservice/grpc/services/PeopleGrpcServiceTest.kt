package com.benwanlh.peopleservice.grpc.services

import com.benwanlh.people.v1.batchGetPeopleRequest
import com.benwanlh.people.v1.createPersonRequest
import com.benwanlh.people.v1.getPersonRequest
import com.benwanlh.people.v1.updatePersonRequest
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
class PeopleGrpcServiceTest @Autowired constructor(
    private val peopleGrpcService: PeopleGrpcService,
) {

    @Test
    fun `getPerson fails with NOT_FOUND when id does not exist`() = runTest {
        val ex = assertFailsWith<StatusException> {
            peopleGrpcService.getPerson(getPersonRequest { id = 999_999L })
        }

        assertEquals(Status.Code.NOT_FOUND, ex.status.code)
    }

    @Test
    fun `getPerson returns when exist`() = runTest {
        val proto = peopleGrpcService.createPerson(createPersonRequest { name = "test" })

        assertEquals(proto.id, peopleGrpcService.getPerson(getPersonRequest { id = proto.id }).id)
    }

    @Test
    fun `createPerson fails with Invalid Argument if name is blank`() = runTest {
        val ex = assertFailsWith<StatusException> {
            peopleGrpcService.createPerson(createPersonRequest {name ="" })
        }

        assertEquals(Status.Code.INVALID_ARGUMENT, ex.status.code)
    }

    @Test
    fun `createPerson fails with Invalid Argument if birthDate is in the future`() = runTest {
        val ex = assertFailsWith<StatusException> {
            val tomorrow = LocalDate.now().plusDays(1).toString()   // e.g. "2026-10-31"
            peopleGrpcService.createPerson(createPersonRequest {
                name ="ben"
                birthDate = tomorrow
            })
        }

        assertEquals(Status.Code.INVALID_ARGUMENT, ex.status.code)
    }

    @Test
    fun `createPerson fails with Invalid Argument if birthDate is not a date`() = runTest {
        val ex = assertFailsWith<StatusException> {
            peopleGrpcService.createPerson(createPersonRequest {
                name ="ben"
                birthDate = "abc"
            })
        }

        assertEquals(Status.Code.INVALID_ARGUMENT, ex.status.code)
    }

    @Test
    fun `updatePerson fails with NOT_FOUND when id is not found`() = runTest {
        val ex = assertFailsWith<StatusException> {
            peopleGrpcService.updatePerson(updatePersonRequest {
                id = 123L
                name = "test"
            })
        }

        assertEquals(Status.Code.NOT_FOUND, ex.status.code)
    }

    @Test
    fun `batchGetPeople does not fail with duplicate or unknown id`() = runTest {
        val proto = peopleGrpcService.createPerson(createPersonRequest { name = "test" })

        val protos = peopleGrpcService.batchGetPeople(batchGetPeopleRequest { ids += listOf(proto.id, proto.id, proto.id+1L) });
        assertEquals(1, protos.peopleCount)
    }

    @Test
    fun `batchGetPeople returns emptyList if id not found`() = runTest {

        val protos = peopleGrpcService.batchGetPeople(batchGetPeopleRequest { ids += listOf(999L) });
        assertEquals(0, protos.peopleCount)
    }
}