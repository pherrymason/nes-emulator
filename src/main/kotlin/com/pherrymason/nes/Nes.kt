package com.pherrymason.nes

import com.pherrymason.nes.cpu.CPU6502

@ExperimentalUnsignedTypes
class Nes {
    val cpu = CPU6502(RAM());

    fun run() {
        cpu.reset()
        cpu.clock();
    }
}