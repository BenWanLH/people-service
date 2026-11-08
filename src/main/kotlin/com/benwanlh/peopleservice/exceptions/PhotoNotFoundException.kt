package com.benwanlh.peopleservice.exceptions

class PhotoNotFoundException (val id: Long) : RuntimeException("Photo not found for Person $id")