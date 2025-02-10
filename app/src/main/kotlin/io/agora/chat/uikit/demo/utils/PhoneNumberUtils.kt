package io.agora.chat.uikit.demo.utils

import java.util.regex.Pattern

object PhoneNumberUtils {
    /**
     * Mobile phone number verification
     * @param phone
     * @return
     */
    fun isPhoneNumber(phone: String?): Boolean {
        return Pattern.matches("^1\\d{10}$", phone)
    }

    fun isNumber(number: String?): Boolean {
        return Pattern.matches("^([1-9]\\d*)|(0)$", number)
    }
}