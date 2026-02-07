package com.naqiran.validators

import io.kotest.core.Tuple2

object TestModels {
    val numbersTestSample =
        mapOf<String, Tuple2<Number, Number>>(
            "double" to Tuple2(1.0, 0.0),
            "int" to Tuple2(1, 0),
            "long" to Tuple2(1L, 0L),
            "float" to Tuple2(1F, 0F),
        )
}
