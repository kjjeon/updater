package com.altimedia.updater.data.model

import java.io.Serializable

class PayloadSpec private constructor(
    val url: String,
    val offset: Long,
    val size: Long,
    val properties: List<String>) : Serializable {


    private constructor(builder: Builder) : this(builder.url, builder.offset, builder.size, builder.properties)

    companion object {
        fun build(init: Builder.() -> Unit) = Builder(
            init
        ).build()
    }

    class Builder private constructor() {
        lateinit var url: String
        var offset: Long = 0
        var size: Long = 0
        lateinit var properties: List<String>

        constructor(init: Builder.() -> Unit) : this() {
            init()
        }

        fun url(init: Builder.() -> String) = apply { url = init() }

        fun offset(init: Builder.() -> Long) = apply { offset = init() }

        fun size(init: Builder.() -> Long) = apply { size = init() }

        fun properties(init: Builder.() -> List<String>) = apply { properties = init() }

        fun build() = PayloadSpec(this)
    }
}

