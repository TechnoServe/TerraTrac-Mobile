package org.technoserve.farmcollector.database

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

object Validator {

    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    private val PHONE_NUMBER_REGEX = "^\\+?[0-9]{10,15}$".toRegex() // Supports optional '+' and 10-15 digits
    private val NAME_REGEX = "^[A-Za-z\\s'-]+$".toRegex() // Allows letters, spaces, apostrophes, and hyphens

    fun isValidEmail(email: String?): Boolean {
        return email != null && EMAIL_REGEX.matches(email)
    }

    fun isValidPhoneNumber(phoneNumber: String?): Boolean {
        return phoneNumber != null && PHONE_NUMBER_REGEX.matches(phoneNumber)
    }

    fun isValidName(name: String?): Boolean {
        return name != null && NAME_REGEX.matches(name)
    }
}

class ValidatorTest {

    @Test
    fun emailValidator_CorrectEmail_ReturnsTrue() {
        assertTrue(Validator.isValidEmail("name@email.com"))
        assertTrue(Validator.isValidEmail("example.name+123@gmail.com"))
    }

    @Test
    fun emailValidator_IncorrectEmail_ReturnsFalse() {
        assertFalse(Validator.isValidEmail("nameemail.com"))
        assertFalse(Validator.isValidEmail("name.com"))
        assertFalse(Validator.isValidEmail(null))
    }

    @Test
    fun phoneNumberValidator_CorrectPhoneNumber_ReturnsTrue() {
        assertTrue(Validator.isValidPhoneNumber("+1234567890"))
        assertTrue(Validator.isValidPhoneNumber("1234567890"))
        assertTrue(Validator.isValidPhoneNumber("+123456789012345"))
    }

    @Test
    fun phoneNumberValidator_IncorrectPhoneNumber_ReturnsFalse() {
        assertFalse(Validator.isValidPhoneNumber("12345")) // Too short
        assertFalse(Validator.isValidPhoneNumber("+12345abcde")) // Contains letters
        assertFalse(Validator.isValidPhoneNumber(null)) // Null
    }

    @Test
    fun nameValidator_CorrectName_ReturnsTrue() {
        assertTrue(Validator.isValidName("John Doe"))
        assertTrue(Validator.isValidName("O'Connor"))
        assertTrue(Validator.isValidName("Anne-Marie"))
    }

    @Test
    fun nameValidator_IncorrectName_ReturnsFalse() {
        assertFalse(Validator.isValidName("John123")) // Contains numbers
        assertFalse(Validator.isValidName("!@#$%^&*()")) // Contains special characters
        assertFalse(Validator.isValidName(null)) // Null
    }
}



