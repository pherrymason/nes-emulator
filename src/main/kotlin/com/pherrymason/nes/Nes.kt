package com.pherrymason.nes

import com.pherrymason.nes.cpu.Cpu

@ExperimentalUnsignedTypes
class Nes {
    val cpu = Cpu(RAM());

    fun run() {
        cpu.reset()
        cpu.clock();
    }
}