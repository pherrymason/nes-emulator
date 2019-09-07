package com.pherrymason.nes

@ExperimentalUnsignedTypes
data class NesByte constructor(val byte: UByte) {
    constructor(other: Int) : this(other.toUByte())

    infix operator fun plus(other: NesByte): NesByte = NesByte(byte.plus(other.byte).toUByte())
    infix operator fun plus(other: Int): NesByte = NesByte(this.byte.plus(other.toUInt()).toUByte())
    infix operator fun minus(other: Int): NesByte = NesByte(byte.minus(other.toUInt()).toUByte())

    infix fun and(other: Int): NesByte = NesByte(byte.toInt().and(other))
    infix fun and(other: NesByte): NesByte = NesByte(byte.and(other.byte))
    infix fun or(other: NesByte): NesByte = NesByte(byte.or(other.byte))

    fun shl(bitCount: Int): NesByte {
        return NesByte(byte.toInt().shl(bitCount))
    }

    fun shr(bitCount: Int): NesByte {
        return NesByte(byte.toInt().shr(bitCount))
    }

    fun inv(): NesByte {
        return NesByte(byte.inv())
    }

    fun toWord(): Word {
        return Word(this.byte.toUShort())
    }

    fun toUShort(): UShort {
        return this.byte.toUShort()
    }

    fun toInt(): Int {
        return this.byte.toInt()
    }

    infix operator fun compareTo(other: NesByte): Int {
        return byte.compareTo(other.byte)
    }
    infix operator fun compareTo(other: Int): Int {
        return byte.compareTo(other.toUByte())
    }

    override infix operator fun equals(other: Any?): Boolean {
        val hashA = byte.hashCode()
        val hashB = other.hashCode()
        return hashA == hashB
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

    infix operator fun plus(other: NesByte): Word {
        return Word(this.word.toShort().plus(other.byte.toByte()))
    }
}

@ExperimentalUnsignedTypes
typealias Address = Word

@ExperimentalUnsignedTypes
typealias ProgramCounter = Word
