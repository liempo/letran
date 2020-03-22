package com.liempo.letran.auth

object VerificationUtils {

    /**
     * Original plan is to verify student number using the
     * Letran database but MIS denied access to it so as a compromise,
     * I have set up the following rules:
     *
     * 1. The student's ID must be scanned within the app using the camera
     * 2. Barcode scanned must be 7 digits and does not contain any characters
     * 3. First number is student category (1 - elementary, 2 - high school, 3 - college)
     * 4. Second and third number the year, I'll assume that students who will use this
     *      app studied within  2000-2020, making the second and third number only from 00-20
     *
     * NOTE: This is not a good way to deal with this
     *       problem since it can be exploited in many ways
     **/
    internal fun verifyAll(value: String?): Boolean =
        value != null && checkDigitCount(value) &&
                checkCategory(value) && checkYear(value)

    private fun checkDigitCount(value: String?): Boolean =
        value != null && value.length == 7
                && value.all { it.isDigit() }

    private fun checkCategory(value: String?): Boolean =
        value != null && Character.getNumericValue(value.first()) in 1..3

    private fun checkYear(value: String?): Boolean =
        value != null && value.substring(1, 3).toInt() in 0..20
}