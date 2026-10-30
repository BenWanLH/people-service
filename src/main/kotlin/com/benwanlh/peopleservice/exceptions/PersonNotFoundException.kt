package com.benwanlh.peopleservice.exceptions

class PersonNotFoundException(val id: Long) : RuntimeException("Person $id not found")