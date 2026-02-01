package com.naqiran.validators

import java.math.BigDecimal
import java.util.UUID

@DslMarker annotation class ValidatorDsl

@ValidatorDsl
class ApplicationValidator {
    private val messageList = mutableListOf<ConstraintViolation>()
    private val key: String

    constructor(key: String? = UUID.randomUUID().toString()) {
        this.key = key.orEmpty().ifBlank { UUID.randomUUID().toString() }
    }

    fun check(
        predicate: Boolean,
        message: () -> String,
    ) {
        if (!predicate) {
            messageList += ConstraintViolation(key, message())
        }
    }

    fun checkNotNull(
        value: Any?,
        message: () -> String,
    ) = check(value != null, message)

    fun checkGreater(
        value: BigDecimal?,
        threshold: BigDecimal,
        message: () -> String,
    ) = check(value != null && value > threshold, message)

    fun checkBlank(
        value: String?,
        message: () -> String,
    ) = check(value.isNullOrBlank(), message)

    fun checkNotBlank(
        value: String?,
        message: () -> String,
    ) = check(!value.isNullOrBlank(), message)

    fun checkLength(
        value: String?,
        maxLength: Int,
        message: () -> String,
    ) = check(value.orEmpty().length.let { length -> length in 1..maxLength }, message)

    fun checkNull(
        value: Any?,
        message: () -> String,
    ) = check(value == null, message)

    fun <T> checkAll(
        listValue: Iterable<T>?,
        predicate: (T) -> Boolean,
        message: () -> String,
    ) = check(listValue?.all { predicate(it) } == true, message)

    fun <T> checkEqualsAndNotNull(
        first: T?,
        second: T?,
        message: () -> String,
    ) = check(first != null && second != null && first == second, message)

    fun <T> checkIn(
        value: T?,
        values: Iterable<T>,
        message: () -> String,
    ) = check(value in values, message)

    fun <T : Enum<T>> checkIn(
        value: String?,
        clazz: Class<T>,
        message: () -> String,
    ) = check(clazz.enumConstants.any { it.name == value }) {
        "${message()} and valid values are ${clazz.enumConstants.joinToString(", ") { it.name }}"
    }

    fun checkEquals(
        value1: BigDecimal?,
        value2: BigDecimal?,
        message: () -> String,
    ) = check(value1 != null && value2 != null && value1.compareTo(value2) == 0, message)

    fun fail(message: () -> String) = check(false, message)

    fun addAll(validators: List<ApplicationValidator>?) =
        validators?.reduceOrNull { acc, validator -> acc.add(validator) }?.let { this.add(it) }
            ?: this

    fun add(validator: ApplicationValidator): ApplicationValidator =
        let {
            this += validator
            this
        }

    private operator fun plusAssign(validator: ApplicationValidator) {
        this.messageList += validator.get()
    }

    fun get(): List<ConstraintViolation> = messageList

    fun <R> map(transform: (List<ConstraintViolation>) -> R): R = transform(messageList)

    fun toResult(message: String? = null): Result<Boolean> =
        this.map {
            if (it.isEmpty()) {
                Result.success(true)
            } else {
                val errorMessage =
                    message ?: it.joinToString { violation -> "${violation.key} - ${violation.message}" }
                Result.failure(ValidationException(errorMessage))
            }
        }

    data class ConstraintViolation(
        val key: String = UUID.randomUUID().toString(),
        val message: String,
        val messageType: ViolationType = ViolationType.ERROR,
    ) {
        enum class ViolationType {
            ERROR,
        }
    }

    class ValidationException(
        message: String,
    ) : Exception(message)
}

fun ApplicationValidator?.orValid(): ApplicationValidator = this ?: validate {}

fun ApplicationValidator?.orInValid(message: () -> String) = this ?: validate { fail { message() } }

fun List<ApplicationValidator>?.reduceValid() = validate {}.addAll(this)

fun List<ApplicationValidator>?.reduceInvalid(message: () -> String) =
    if (this.isNullOrEmpty()) {
        validate { fail { message() } }
    } else {
        this.reduceValid()
    }

inline fun validate(
    key: String? = null,
    block: ApplicationValidator.() -> Unit,
): ApplicationValidator {
    val validator = ApplicationValidator(key)
    validator.block()
    return validator
}
