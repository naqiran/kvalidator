package com.naqiran.validators

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ApplicationValidatorTest :
    FunSpec({
        test("toResult returns success when no violations") {
            validate("ok") {
                checkNotBlank("value") { "should not be blank" }
                checkLength("value", 10) { "too long" }
            }.toResult()
                .shouldBeSuccess()
        }

        test("toResult includes key and message in ValidationException") {
            validate("field") { checkNotNull(null) { "missing" } }
                .toResult()
                .shouldBeFailure { it.message.shouldBe("field - missing") }
        }

        test("checkLength fails when empty or above max length") {
            validate("length") {
                checkLength("", 3) { "invalid length" }
                checkLength("abcd", 3) { "invalid length" }
            }.toResult()
                .shouldBeFailure {
                    it.message.shouldBe("length - invalid length, length - invalid length")
                }
        }

        test("checkIn enum includes valid values in message") {
            validate("color") { checkIn("BLUE", Color::class.java) { "invalid color" } }
                .toResult()
                .shouldBeFailure {
                    it.message.shouldBe("color - invalid color and valid values are RED, GREEN")
                }
        }

        test("addAll combines violations from multiple validators") {
            val first = validate("a") { check(false) { "bad a" } }
            val second = validate("b") { check(false) { "bad b" } }

            validate("root") {}
                .addAll(listOf(first, second))
                .toResult()
                .shouldBeFailure { it.message.shouldBe("a - bad a, b - bad b") }
        }

        test("reduceInvalid returns failure for empty list") {
            emptyList<ApplicationValidator>()
                .reduceInvalid { "missing validators" }
                .toResult()
                .shouldBeFailure { it.message.shouldContain("missing validators") }
        }
    })

enum class Color {
    RED,
    GREEN,
}
