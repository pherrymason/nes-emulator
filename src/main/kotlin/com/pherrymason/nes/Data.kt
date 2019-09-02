package com.pherrymason.nes

import kotlin.Byte as OriginalByte

@ExperimentalUnsignedTypes
class Byte constructor(val byte: UByte) {
    constructor(other: Int) : this(other.toUByte())

    infix operator fun plus(other: Byte): Byte = Byte(byte.plus(other.byte).toUByte())
    infix operator fun plus(other: Int): Byte = Byte(this.byte.plus(other.toUInt()).toUByte())
    infix operator fun minus(other: Int): Byte = Byte(byte.minus(other.toUInt()).toUByte())

    infix fun and(other: Int): Byte = Byte(byte.toInt().and(other))

    fun toWord(): Word {
        return Word(this.byte.toUShort())
    }

    fun toUShort(): UShort {
        return this.byte.toUShort()
    }

    fun toInt(): Int {
        return this.byte.toInt()
    }

    infix operator fun compareTo(other: Byte): Int = byte.compareTo(other.byte)
    infix operator fun compareTo(other: Int): Int = byte.compareTo(other.toUByte())
}

@ExperimentalUnsignedTypes
data class Word constructor(val word: UShort) {
    constructor(lo: Byte, hi: Byte) : this(hi.toInt().shl(8).or(lo.toInt()))
    constructor(lo: Byte, hi: Int) : this(hi.shl(8).or(lo.toInt()))
    constructor(word: Int) : this(word.toUShort())

    fun shl(bitCount: Int): Word {
        return Word(this.word.toInt().shl(bitCount))
    }

    fun shr(bitCount: Int): Word {
        return Word(this.word.toInt().shr(bitCount))
    }

    infix fun or(other: Int): Word {
        return Word(this.word.toInt() or other)
    }

    fun and(other: Int): Word {
        return Word(this.word.toInt() and other)
    }

    infix operator fun minus(other: Int): Word = Word(word.toShort().minus(other).toUShort())

    infix operator fun plus(other: Int): Word {
        return Word(this.word.toShort().plus(other).toUShort())
    }

    infix operator fun plus(other: Byte): Word {
        return Word(this.word.toShort().plus(other.byte.toByte()))
    }
}

@ExperimentalUnsignedTypes
typealias Address = Word

@ExperimentalUnsignedTypes
typealias ProgramCounter = Word
