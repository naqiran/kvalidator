package com.naqiran.validators

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withContexts
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ApplicationValidatorTest :
    FunSpec({
        test("toResult returns success when no violations") {
            validateToResult("ok") {
                checkNotBlank("value") { "should not be blank" }
                checkLength("value", 10) { "too long" }
            }.shouldBeSuccess()
        }

        test("toResult includes key and message in ValidationException") {
            validateToResult("field") { checkNotNull(null) { "missing" } }
                .shouldBeFailure { it.message.shouldBe("field - missing") }
        }

        test("checkLength fails when empty or above max length") {
            validateToResult("length") {
                checkLength("", 3) { "invalid length" }
                checkLength("abcd", 3) { "invalid length" }
            }.shouldBeFailure { it.message.shouldBe("length - invalid length") }
        }

        test("checkIn enum includes valid values in message") {
            validateToResult("color") { checkIsIn("BLUE", Color::class.java) { "invalid color" } }
                .shouldBeFailure {
                    it.message.shouldBe("color - invalid color and valid values are RED, GREEN")
                }
        }

        test("reduceInvalid returns failure for empty list") {
            emptyList<ApplicationValidator>()
                .reduceInvalid { "missing validators" }
                .toResult()
                .shouldBeFailure { it.message.shouldContain("missing validators") }
        }

        test("check") {
            validateToResult("failed") { check(false) { "This is failure test case" } }
                .shouldBeFailure { it.message.shouldBe("failed - This is failure test case") }
            validateToResult("ok") { check(true) { "This is successful test case" } }.shouldBeSuccess()
        }

        test("checkNotNull") {
            val someValue = "Test Value"
            val nullValue = null
            validateToResult("failed") { checkNotNull(nullValue) { "This is failure test case" } }
                .shouldBeFailure { it.message.shouldBe("failed - This is failure test case") }
            validateToResult("ok") { checkNotNull(someValue) { "This is successful test case" } }
                .shouldBeSuccess()
        }

        context("Greater Than Check") {
            withContexts(TestModels.numbersTestSample) { value ->
                validateToResult("ok") {
                    checkGreater(value.component1(), value.component2()) {
                        "Greater validation success"
                    }
                }.shouldBeSuccess()
                validateToResult("failure") {
                    checkGreater(value.component2(), value.component1()) {
                        "Greater validation failure"
                    }
                }.shouldBeFailure { it.message.shouldContain("failure - Greater validation failure") }
            }
        }

        context("Lesser Than Check") {
            withContexts(TestModels.numbersTestSample) { value ->
                validateToResult("ok") {
                    checkLesser(value.component2(), value.component1()) { "Greater validation success" }
                }.shouldBeSuccess()
                validateToResult("failure") {
                    checkLesser(value.component1(), value.component2()) { "Greater validation failure" }
                }.shouldBeFailure { it.message.shouldContain("failure - Greater validation failure") }
            }
        }
    })

enum class Color {
    RED,
    GREEN,
}
