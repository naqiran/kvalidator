package com.naqiran.validators.example

import com.naqiran.validators.reduceInvalid
import com.naqiran.validators.validate
import java.math.BigDecimal

fun Order.validate() =
    validate("Order Id: $orderId") {
        checkNotNull(orderId) { "Order ID must not be null" }
        checkGreater(amount, 0.toBigDecimal()) { "Order Amount must be greater than zero" }
        add(lines.validate())  //Desired
        lines.validate() //Undesired
        notAValidatorMethod() //Undesired
    }

fun List<Order.OrderLine>?.validate() = this?.map { it.validate() }.reduceInvalid { "Lines cannot be empty" }

fun Order.OrderLine.validate() =
    validate("Line Id: $lineId") {
        checkNotNull(lineId) { "Order Line ID must not be null" }
        checkNotNull(productId) { "Product ID must not be null" }
        checkNotNull(quantity) { "Quantity must not be null" }
        checkNotNull(unitPrice) { "Unit Price must not be null" }
        checkNotNull(lineAmount) { "Line Amount must not be null" }
    }

fun notAValidatorMethod(): String {
    return "Not a validator"
}

data class Order(
    var orderId: String? = null,
    var amount: BigDecimal? = null,
    var lines: List<OrderLine>? = null,
) {
    data class OrderLine(
        var lineId: String? = null,
        var productId: String? = null,
        var quantity: Int? = null,
        var unitPrice: BigDecimal? = null,
        var lineAmount: BigDecimal? = null,
    )
}
