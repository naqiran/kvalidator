package com.naqiran.validators

import java.net.URL
import java.time.LocalDate
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
        value: Number?,
        threshold: Number,
        message: () -> String,
    ) = check(value != null && value.toDouble() > threshold.toDouble(), message)

    fun checkGreaterOrEqual(
        value: Number?,
        threshold: Number,
        message: () -> String,
    ) = check(value != null && value.toDouble() >= threshold.toDouble(), message)

    fun checkLesser(
        value: Number?,
        threshold: Number,
        message: () -> String,
    ) = check(value != null && value.toDouble() < threshold.toDouble(), message)

    fun checkLesserOrEqual(
        value: Number?,
        threshold: Number,
        message: () -> String,
    ) = check(value != null && value.toDouble() <= threshold.toDouble(), message)

    fun checkEquals(
        value1: Number?,
        value2: Number,
        message: () -> String,
    ) = check(value1 != null && value1.toDouble() == value2.toDouble(), message)

    fun checkBlank(
        value: String?,
        message: () -> String,
    ) = check(value.isNullOrBlank(), message)

    fun checkNotBlank(
        value: String?,
        message: () -> String,
    ) = check(!value.isNullOrBlank(), message)

    fun checkMaxLength(
        value: String?,
        maxLength: Int,
        message: () -> String,
    ) = check(value.orEmpty().length <= maxLength, message)

    fun checkMinLength(
        value: String?,
        minLength: Int,
        message: () -> String,
    ) = check(value.orEmpty().length >= minLength, message)

    fun checkExactLength(
        value: String?,
        length: Int,
        message: () -> String,
    ) = check(value.orEmpty().length == length, message)

    fun checkContains(
        value: String?,
        fragment: String,
        message: () -> String,
    ) = check(value.orEmpty().contains(fragment), message)

    fun checkStartsWith(
        value: String?,
        prefix: String,
        message: () -> String,
    ) = check(value.orEmpty().startsWith(prefix), message)

    fun checkEndsWith(
        value: String?,
        suffix: String,
        message: () -> String,
    ) = check(value.orEmpty().endsWith(suffix), message)

    fun checkMatchesRegex(
        value: String?,
        regex: Regex,
        message: () -> String,
    ) = check(value != null && regex.matches(value), message)

    fun checkMatchesAnyRegex(
        value: String?,
        regexes: Iterable<Regex>,
        message: () -> String,
    ) = check(value != null && regexes.any { it.matches(value) }, message)

    fun checkAlpha(
        value: String?,
        message: () -> String,
    ) = check(value != null && value.matches(Regex("^[A-Za-z]+$")), message)

    fun checkAlphanumeric(
        value: String?,
        message: () -> String,
    ) = check(value != null && value.matches(Regex("^[A-Za-z0-9]+$")), message)

    fun checkInRange(
        value: Number?,
        min: Number,
        max: Number,
        message: () -> String,
    ) = check(
        value != null && value.toDouble() >= min.toDouble() && value.toDouble() <= max.toDouble(),
        message,
    )

    fun checkInRangeExclusive(
        value: Number?,
        min: Number,
        max: Number,
        message: () -> String,
    ) = check(
        value != null && value.toDouble() > min.toDouble() && value.toDouble() < max.toDouble(),
        message,
    )

    fun checkBetween(
        value: Number?,
        min: Number,
        max: Number,
        message: () -> String,
    ) = checkInRange(value, min, max, message)

    fun checkPositive(
        value: Number?,
        message: () -> String,
    ) = check(value != null && value.toDouble() > 0, message)

    fun checkNonNegative(
        value: Number?,
        message: () -> String,
    ) = check(value != null && value.toDouble() >= 0, message)

    fun checkNegative(
        value: Number?,
        message: () -> String,
    ) = check(value != null && value.toDouble() < 0, message)

    fun checkNull(
        value: Any?,
        message: () -> String,
    ) = check(value == null, message)

    fun checkCollectionSize(
        value: Iterable<*>?,
        size: Int,
        message: () -> String,
    ) = check(value?.count() == size, message)

    fun checkNotEmpty(
        value: String?,
        message: () -> String,
    ) = check(!value.isNullOrEmpty(), message)

    fun checkNotEmpty(
        value: Iterable<*>?,
        message: () -> String,
    ) = check(value?.any() == true, message)

    fun checkEmpty(
        value: String?,
        message: () -> String,
    ) = check(value.isNullOrEmpty(), message)

    fun checkEmpty(
        value: Iterable<*>?,
        message: () -> String,
    ) = check(value?.none() == true, message)

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

    fun <T> checkIsIn(
        value: T?,
        values: Iterable<T>,
        message: () -> String,
    ) = check(value in values, message)

    fun <T : Enum<T>> checkEnumMember(
        value: String?,
        clazz: Class<T>,
        message: () -> String,
    ) = check(clazz.enumConstants.any { it.name == value }) {
        "${message()} and valid values are ${clazz.enumConstants.joinToString(", ") { it.name }}"
    }

    fun fail(message: () -> String) = check(false, message)

    fun <T> checkNotIn(
        value: T?,
        values: Iterable<T>,
        message: () -> String,
    ) = check(value !in values, message)

    fun checkUrl(
        value: String?,
        message: () -> String,
    ) = check(value != null && runCatching { URL(value) }.isSuccess, message)

    fun checkBefore(
        date: LocalDate?,
        cutoff: LocalDate,
        message: () -> String,
    ) = check(date != null && date.isBefore(cutoff), message)

    fun checkAfter(
        date: LocalDate?,
        cutoff: LocalDate,
        message: () -> String,
    ) = check(date != null && date.isAfter(cutoff), message)

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

    companion object {
        fun failingValidator(message: String): ApplicationValidator {
            val validator = ApplicationValidator()
            validator.fail { message }
            return validator
        }

        fun successValidator() = ApplicationValidator()
    }
}

fun ApplicationValidator?.orValid(): ApplicationValidator = this ?: ApplicationValidator.successValidator()

fun ApplicationValidator?.orInValid(message: () -> String) = this ?: ApplicationValidator.failingValidator(message())

fun List<ApplicationValidator>?.reduceValid() = ApplicationValidator.successValidator().addAll(this)

fun List<ApplicationValidator>?.reduceInvalid(message: () -> String) =
    if (this.isNullOrEmpty()) {
        ApplicationValidator.failingValidator(message())
    } else {
        this.reduceValid()
    }

inline fun validate(
    key: String? = null,
    block: ApplicationValidator.() -> Unit,
) = ApplicationValidator(key).let {
    block(it)
    it
}

inline fun validateToResult(
    key: String? = null,
    block: ApplicationValidator.() -> Unit,
) = validate(key, block).toResult()

inline fun validateAndThrow(
    key: String? = null,
    block: ApplicationValidator.() -> Unit,
) = validateToResult(key, block).getOrThrow()
