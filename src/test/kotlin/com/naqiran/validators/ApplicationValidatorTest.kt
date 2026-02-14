package com.naqiran.validators

import io.kotest.assertions.throwables.shouldThrow
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
                checkMaxLength("value", 10) { "too long" }
            }.shouldBeSuccess()
        }

        test("toResult includes key and message in ValidationException") {
            validateToResult("field") { checkNotNull(null) { "missing" } }
                .shouldBeFailure { it.message.shouldBe("field - missing") }
        }

        test("checkLength fails when empty or above max length") {
            validateToResult("length") {
                checkMaxLength("", 3) { "invalid length" }
                checkMaxLength("abcd", 3) { "invalid length" }
            }.shouldBeFailure { it.message.shouldBe("length - invalid length") }
            validateToResult("length") { checkMaxLength("", 3) { "invalid length" } }.shouldBeSuccess()
        }

        test("checkIn enum includes valid values in message") {
            validateToResult("color") { checkEnumMember("BLUE", Color::class.java) { "invalid color" } }
                .shouldBeFailure {
                    it.message.shouldBe("color - invalid color and valid values are RED, GREEN")
                }
            validateToResult("color") { checkEnumMember("RED", Color::class.java) { "invalid color" } }
                .shouldBeSuccess()
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

        test("checkGreaterOrEqual succeeds when equal") {
            validateToResult("ok") { checkGreaterOrEqual(5, 5) { "must be >= 5" } }.shouldBeSuccess()
            validateToResult("fail") { checkGreaterOrEqual(4, 5) { "must be >= 5" } }
                .shouldBeFailure { it.message.shouldBe("fail - must be >= 5") }
        }

        test("checkLesserOrEqual succeeds when equal") {
            validateToResult("ok") { checkLesserOrEqual(5, 5) { "must be <= 5" } }.shouldBeSuccess()
            validateToResult("fail") { checkLesserOrEqual(6, 5) { "must be <= 5" } }
                .shouldBeFailure { it.message.shouldBe("fail - must be <= 5") }
        }

        test("checkEquals compares numbers by value") {
            validateToResult("ok") { checkEquals(1, 1.0) { "must equal" } }.shouldBeSuccess()
            validateToResult("fail") { checkEquals(null, 1) { "must equal" } }
                .shouldBeFailure { it.message.shouldBe("fail - must equal") }
        }

        test("checkBlank and checkNotBlank") {
            validateToResult("ok") { checkBlank("  ") { "must be blank" } }.shouldBeSuccess()
            validateToResult("fail") { checkBlank("text") { "must be blank" } }
                .shouldBeFailure { it.message.shouldBe("fail - must be blank") }
            validateToResult("ok") { checkNotBlank("text") { "must not be blank" } }.shouldBeSuccess()
            validateToResult("fail") { checkNotBlank("") { "must not be blank" } }
                .shouldBeFailure { it.message.shouldBe("fail - must not be blank") }
        }

        test("checkMinLength") {
            validateToResult("ok") { checkMinLength("abcd", 4) { "too short" } }.shouldBeSuccess()
            validateToResult("fail") { checkMinLength("abc", 4) { "too short" } }
                .shouldBeFailure { it.message.shouldBe("fail - too short") }
        }

        test("checkExactLength") {
            validateToResult("ok") { checkExactLength("abcd", 4) { "wrong length" } }.shouldBeSuccess()
            validateToResult("fail") { checkExactLength("abc", 4) { "wrong length" } }
                .shouldBeFailure { it.message.shouldBe("fail - wrong length") }
        }

        test("checkContains") {
            validateToResult("ok") { checkContains("kotlin", "lin") { "missing fragment" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkContains("kotlin", "java") { "missing fragment" } }
                .shouldBeFailure { it.message.shouldBe("fail - missing fragment") }
        }

        test("checkStartsWith") {
            validateToResult("ok") { checkStartsWith("kotlin", "kot") { "wrong prefix" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkStartsWith("kotlin", "lin") { "wrong prefix" } }
                .shouldBeFailure { it.message.shouldBe("fail - wrong prefix") }
        }

        test("checkEndsWith") {
            validateToResult("ok") { checkEndsWith("kotlin", "lin") { "wrong suffix" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkEndsWith("kotlin", "kot") { "wrong suffix" } }
                .shouldBeFailure { it.message.shouldBe("fail - wrong suffix") }
        }

        test("checkMatchesRegex") {
            val digits = Regex("^\\d+$")
            validateToResult("ok") { checkMatchesRegex("12345", digits) { "must be digits" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkMatchesRegex("12a", digits) { "must be digits" } }
                .shouldBeFailure { it.message.shouldBe("fail - must be digits") }
        }

        test("checkMatchesAnyRegex") {
            val patterns = listOf(Regex("^\\d+$"), Regex("^abc$"))
            validateToResult("ok") { checkMatchesAnyRegex("abc", patterns) { "no match" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkMatchesAnyRegex("a1", patterns) { "no match" } }
                .shouldBeFailure { it.message.shouldBe("fail - no match") }
        }

        test("checkAlpha") {
            validateToResult("ok") { checkAlpha("abcDEF") { "alpha only" } }.shouldBeSuccess()
            validateToResult("fail") { checkAlpha("abc1") { "alpha only" } }
                .shouldBeFailure { it.message.shouldBe("fail - alpha only") }
        }

        test("checkAlphanumeric") {
            validateToResult("ok") { checkAlphanumeric("abc123") { "alphanumeric only" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkAlphanumeric("abc-123") { "alphanumeric only" } }
                .shouldBeFailure { it.message.shouldBe("fail - alphanumeric only") }
        }

        test("checkInRange") {
            validateToResult("ok") { checkInRange(5, 1, 5) { "out of range" } }.shouldBeSuccess()
            validateToResult("fail") { checkInRange(6, 1, 5) { "out of range" } }
                .shouldBeFailure { it.message.shouldBe("fail - out of range") }
        }

        test("checkInRangeExclusive") {
            validateToResult("ok") { checkInRangeExclusive(1, 0, 2) { "out of range" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkInRangeExclusive(0, 0, 2) { "out of range" } }
                .shouldBeFailure { it.message.shouldBe("fail - out of range") }
        }

        test("checkBetween uses inclusive range") {
            validateToResult("ok") { checkBetween(5, 1, 5) { "out of range" } }.shouldBeSuccess()
            validateToResult("fail") { checkBetween(6, 1, 5) { "out of range" } }
                .shouldBeFailure { it.message.shouldBe("fail - out of range") }
        }

        test("checkNotIn – failure and success") {
            validateToResult("fail") { checkNotIn("A", listOf("A", "B", "C")) { "value not allowed" } }
                .shouldBeFailure { it.message.shouldBe("fail - value not allowed") }
            validateToResult("ok") { checkNotIn("Z", listOf("A", "B", "C")) { "value not allowed" } }
                .shouldBeSuccess()
        }

        test("checkUrl – valid and invalid URL") {
            validateToResult("ok") { checkUrl("https://example.com") { "bad URL" } }.shouldBeSuccess()
            validateToResult("fail") { checkUrl("not a url") { "bad URL" } }
                .shouldBeFailure { it.message.shouldBe("fail - bad URL") }
        }

        test("checkBefore and checkAfter – date comparisons") {
            val today = java.time.LocalDate.of(2024, 2, 14)
            val tomorrow = today.plusDays(1)
            val yesterday = today.minusDays(1)
            validateToResult("ok") { checkBefore(yesterday, today) { "date too late" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkBefore(today, today) { "date too late" } }
                .shouldBeFailure { it.message.shouldBe("fail - date too late") }
            validateToResult("ok") { checkAfter(tomorrow, today) { "date too early" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkAfter(today, today) { "date too early" } }
                .shouldBeFailure { it.message.shouldBe("fail - date too early") }
        }
        test("checkPositive, checkNonNegative, and checkNegative") {
            validateToResult("ok") { checkPositive(1) { "must be positive" } }.shouldBeSuccess()
            validateToResult("fail") { checkPositive(0) { "must be positive" } }
                .shouldBeFailure { it.message.shouldBe("fail - must be positive") }
            validateToResult("ok") { checkNonNegative(0) { "must be non-negative" } }.shouldBeSuccess()
            validateToResult("fail") { checkNonNegative(-1) { "must be non-negative" } }
                .shouldBeFailure { it.message.shouldBe("fail - must be non-negative") }
            validateToResult("ok") { checkNegative(-1) { "must be negative" } }.shouldBeSuccess()
            validateToResult("fail") { checkNegative(0) { "must be negative" } }
                .shouldBeFailure { it.message.shouldBe("fail - must be negative") }
        }

        test("checkCollectionSize") {
            validateToResult("ok") { checkCollectionSize(listOf(1, 2), 2) { "wrong size" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkCollectionSize(listOf(1, 2), 3) { "wrong size" } }
                .shouldBeFailure { it.message.shouldBe("fail - wrong size") }
        }

        test("checkNotEmpty for strings and collections") {
            validateToResult("ok") { checkNotEmpty("value") { "empty" } }.shouldBeSuccess()
            validateToResult("fail") { checkNotEmpty("") { "empty" } }
                .shouldBeFailure { it.message.shouldBe("fail - empty") }
            validateToResult("ok") { checkNotEmpty(listOf(1)) { "empty" } }.shouldBeSuccess()
            validateToResult("fail") { checkNotEmpty(emptyList<Int>()) { "empty" } }
                .shouldBeFailure { it.message.shouldBe("fail - empty") }
        }

        test("checkEmpty for strings and collections") {
            validateToResult("ok") { checkEmpty("") { "not empty" } }.shouldBeSuccess()
            validateToResult("fail") { checkEmpty("value") { "not empty" } }
                .shouldBeFailure { it.message.shouldBe("fail - not empty") }
            validateToResult("ok") { checkEmpty(emptyList<Int>()) { "not empty" } }.shouldBeSuccess()
            validateToResult("fail") { checkEmpty(listOf(1)) { "not empty" } }
                .shouldBeFailure { it.message.shouldBe("fail - not empty") }
        }

        test("checkNull") {
            validateToResult("ok") { checkNull(null) { "must be null" } }.shouldBeSuccess()
            validateToResult("fail") { checkNull("value") { "must be null" } }
                .shouldBeFailure { it.message.shouldBe("fail - must be null") }
        }

        test("checkAll requires all elements to match") {
            validateToResult("ok") { checkAll(listOf(2, 4, 6), { it % 2 == 0 }) { "all even" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkAll(listOf(2, 3, 6), { it % 2 == 0 }) { "all even" } }
                .shouldBeFailure { it.message.shouldBe("fail - all even") }
        }

        test("checkEqualsAndNotNull requires both non-null and equal") {
            validateToResult("ok") { checkEqualsAndNotNull("a", "a") { "must match" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkEqualsAndNotNull(null, "a") { "must match" } }
                .shouldBeFailure { it.message.shouldBe("fail - must match") }
        }

        test("checkIsIn validates membership") {
            validateToResult("ok") { checkIsIn("a", listOf("a", "b")) { "not allowed" } }
                .shouldBeSuccess()
            validateToResult("fail") { checkIsIn("c", listOf("a", "b")) { "not allowed" } }
                .shouldBeFailure { it.message.shouldBe("fail - not allowed") }
        }

        test("fail always produces violation") {
            validateToResult("fail") { fail { "forced" } }
                .shouldBeFailure { it.message.shouldBe("fail - forced") }
        }

        test("add and addAll combine violations") {
            val first = validate("first") { check(false) { "one" } }
            val second = validate("second") { check(false) { "two" } }
            val combined = ApplicationValidator().add(first).addAll(listOf(second))
            combined.get().size.shouldBe(2)
        }

        test("map transforms violations") {
            val count = validate("map") { check(false) { "one" } }.map { it.size }
            count.shouldBe(1)
        }

        test("orValid and orInValid provide fallbacks") {
            val valid = (null as ApplicationValidator?).orValid().toResult()
            valid.shouldBeSuccess()
            val invalid = (null as ApplicationValidator?).orInValid { "missing" }.toResult()
            invalid.shouldBeFailure { it.message.shouldContain("missing") }
        }

        test("reduceValid aggregates violations") {
            val validators =
                listOf(
                    validate("first") { check(false) { "one" } },
                    validate("second") { check(false) { "two" } },
                )
            validators
                .reduceValid()
                .get()
                .size
                .shouldBe(2)
        }

        test("validateAndThrow throws when violations exist") {
            shouldThrow<ApplicationValidator.ValidationException> {
                validateAndThrow("throw") { check(false) { "bad" } }
            }
        }
    })

enum class Color {
    RED,
    GREEN,
}
