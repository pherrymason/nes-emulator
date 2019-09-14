package com.pherrymason.nes

@ExperimentalUnsignedTypes
open class NesByte constructor(val byte: UByte) {
    constructor(other: Int) : this(other.toUByte())

    infix operator fun plus(other: NesByte): NesByte = NesByte(byte.plus(other.byte).toUByte())
    infix operator fun plus(other: Int): NesByte = NesByte(this.byte.plus(other.toUInt()).toUByte())
    infix operator fun minus(other: Int): NesByte = NesByte(byte.minus(other.toUInt()).toUByte())
    infix operator fun minus(other: NesByte): NesByte = NesByte(byte.minus(other.byte).toUByte())
    operator fun inc(): NesByte = NesByte((byte + 1u).toInt())
    operator fun dec(): NesByte = NesByte((byte - 1u).toInt())

    infix fun and(other: Int): NesByte = NesByte(byte.toInt().and(other))
    infix fun and(other: NesByte): NesByte = NesByte(byte.and(other.byte))
    infix fun or(other: NesByte): NesByte = NesByte(byte.or(other.byte))

    fun shl(bitCount: Int): NesByte = NesByte(byte.toInt().shl(bitCount))
    fun shr(bitCount: Int): NesByte = NesByte(byte.toInt().shr(bitCount))
    fun inv(): NesByte = NesByte(byte.inv())


    fun toWord(): Word = Word(this.byte.toUShort())
    fun toUShort(): UShort = this.byte.toUShort()
    fun toInt(): Int = this.byte.toInt()

    infix operator fun compareTo(other: NesByte): Int {
        return byte.compareTo(other.byte)
    }
    infix operator fun compareTo(other: Int): Int {
        return byte.compareTo(other.toUByte())
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NesByte) return false
        return byte == other.byte
    }

    fun toBool(): Boolean {
        return byte != 0u.toUByte()
    }
}

@ExperimentalUnsignedTypes
data class Word constructor(val word: UShort) {
    constructor(lo: NesByte, hi: NesByte) : this(hi.toInt().shl(8).or(lo.toInt()))
    constructor(lo: NesByte, hi: Int) : this(hi.shl(8).or(lo.toInt()))
    constructor(word: Int) : this(word.toUShort())

    fun shl(bitCount: Int): Word = Word(this.word.toInt().shl(bitCount))
    fun shr(bitCount: Int): Word = Word(this.word.toInt().shr(bitCount))

    infix fun or(other: Int): Word = Word(this.word.toInt() or other)
    infix fun and(other: Int): Word = Word(this.word.toInt() and other)

    infix operator fun minus(other: Int): Word = Word(word.toShort().minus(other).toUShort())
    infix operator fun plus(other: Word): Word = Word((this.word + other.word).toUShort())
    infix operator fun plus(other: Int): Word = Word(this.word.toShort().plus(other).toUShort())
    infix operator fun plus(other: NesByte): Word = Word(word.plus(other.byte).toInt())
    operator fun inc(): Word = Word((word + 1u).toUShort())
    operator fun dec(): Word = Word((word - 1u).toUShort())

    fun lowByte(): NesByte {
        return NesByte(this.and(0xFF).word.toUByte())
    }

    fun highByte(): NesByte {
        return NesByte(this.shr(8).word.toUByte())
    }
}

@ExperimentalUnsignedTypes
typealias Address = Word

@ExperimentalUnsignedTypes
typealias ProgramCounter = Word
