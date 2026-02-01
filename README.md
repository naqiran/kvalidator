# Kotlin Validator (kvalidator)

The Validator DSL is a lightweight, Kotlin-based framework for building clear and reusable validation logic in a declarative style. It lets you define validation rules using simple, expressive functions instead of nested if statements.

You can check values for conditions like non-null, non-blank, length limits, numeric comparisons, or membership in enums or lists. Each failed check automatically records a descriptive message, and all results can be combined, summarized, or converted into a success/failure Result.

In short, it provides a clean, composable way to express validation logic that’s easy to read, extend, and maintain.

# Usage

### Simple Validation

```kotlin
val validator = validate("userInfo") {
    checkNotBlank(name) { "Name must be provided." }
    checkGreater(balance, BigDecimal.ZERO) { "Balance must be positive." }
}.toResult().getOrThrow()
```

### Composable Validation
The validation can be composed simply adding a add method to the validator and include other validators.

```kotlin
val componentValidator = validate("userInfo") {
    checkNotBlank(name) { "Name must be provided." }
    checkGreater(balance, BigDecimal.ZERO) { "Balance must be positive." }
}

val aggreatedValidator = validate("OrderInfo") {
    checkNotBlank("Order Id") { "Order Id must be provided" }
    add(componentValidator)
}
```