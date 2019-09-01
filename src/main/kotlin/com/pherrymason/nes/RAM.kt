package com.pherrymason.nes

import com.pherrymason.nes.Byte

@ExperimentalUnsignedTypes
class RAM {
    val memory: MutableMap<Address, Byte> = mutableMapOf()

    fun read(address: Address): Byte {
        return memory.getOrDefault(address, Byte(0x00))
    }

    fun read(address: Int): Byte {
        return memory.getOrDefault(Address(address), Byte(0x00))
    }

    fun write(address: Address, value: Byte) {
        memory[address] = value
    }

    fun write(address: Int, value: Int) {
        memory[Address(address)] = Byte(value)
    }
}