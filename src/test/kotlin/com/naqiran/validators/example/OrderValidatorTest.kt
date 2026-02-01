package com.naqiran.validators.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.string.shouldContain
import java.math.BigDecimal

class OrderValidatorTest :
    FunSpec({
        test("valid order passes validation") {
            Order(
                orderId = "O-1",
                amount = BigDecimal("10.00"),
                lines =
                    listOf(
                        Order.OrderLine(
                            lineId = "L-1",
                            productId = "P-1",
                            quantity = 2,
                            unitPrice = BigDecimal("5.00"),
                            lineAmount = BigDecimal("10.00"),
                        ),
                    ),
            ).validate()
                .toResult()
                .shouldBeSuccess()
        }

        test("missing order id and zero amount produce failures") {
            Order(
                orderId = null,
                amount = BigDecimal.ZERO,
                lines =
                    listOf(
                        Order.OrderLine(
                            lineId = "L-1",
                            productId = "P-1",
                            quantity = 1,
                            unitPrice = BigDecimal("1.00"),
                            lineAmount = BigDecimal("1.00"),
                        ),
                    ),
            ).validate()
                .toResult()
                .shouldBeFailure {
                    it.message.shouldContain("Order Id: null - Order ID must not be null")
                    it.message.shouldContain("Order Id: null - Order Amount must be greater than zero")
                }
        }

        test("missing line fields are reported") {
            Order(
                orderId = "O-2",
                amount = BigDecimal("5.00"),
                lines =
                    listOf(
                        Order.OrderLine(
                            lineId = "L-2",
                            productId = null,
                            quantity = 1,
                            unitPrice = BigDecimal("5.00"),
                            lineAmount = BigDecimal("5.00"),
                        ),
                    ),
            ).validate()
                .toResult()
                .shouldBeFailure {
                    it.message.shouldContain("Line Id: L-2 - Product ID must not be null")
                }
        }

        test("empty lines list fails with lines cannot be empty") {
            Order(
                orderId = "O-3",
                amount = BigDecimal("5.00"),
                lines = emptyList(),
            ).validate()
                .toResult()
                .shouldBeFailure { it.message.shouldContain("Lines cannot be empty") }
        }
    })
