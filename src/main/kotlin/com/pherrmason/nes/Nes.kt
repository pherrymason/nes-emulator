package com.pherrmason.nes

import com.pherrmason.nes.cpu.Cpu

class Nes {
    val cpu = Cpu();

    fun start() {
        cpu.start();
    }
}