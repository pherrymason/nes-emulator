package com.pherrymason.nes.cpu

import com.pherrymason.nes.Byte
import com.pherrymason.nes.Word

@ExperimentalUnsignedTypes
class CpuRegisters {
    // Accumulator
    // and along with the arithmetic logic unit (ALU), supports using the status register for carrying, overflow
    // detection, and so on.
    var a: Byte = Byte(0x00u);

    // Indexes X Y
    // used for several addressing modes. They can be used as loop counters easily, using INC/DEC and branch
    // instructions. Not being the accumulator, they have limited addressing modes themselves when loading and saving.
    var x: Byte = Byte(0x00u);
    var y: Byte = Byte(0x00u);

    // Program Counter:
    // supports 65536 direct (unbanked) memory locations, however not all values are sent to the cartridge.
    // It can be accessed either by allowing CPU's internal fetch logic increment the address bus, an interrupt
    // (NMI, Reset, IRQ/BRQ), and using the RTS/JMP/JSR/Branch instructions.
    var pc: Word = Word(0x0000u);

    // Stack Pointer:
    // can be accessed using interrupts, pulls, pushes, and transfers.
    var s: Byte = Byte(0x00u);

    // Processor Status:
    // This 8-bit register stores the state of the processor. The bits in
    // this register are called flags. Most of the flags have something
    // to do with arithmetic operations.
    //
    // The P register can be read by pushing it on the stack (with PHP or
    // by causing an interrupt). If you only need to read one flag, you
    // can use the branch instructions. Setting the flags is possible by
    // pulling the P register from stack or by using the flag set or
    // clear instructions.
    //var p = 0x00;

    var carryFlag: Boolean = false;
    var zeroFlag: Boolean = false;
    var interruptDisable: Boolean = false;
    var decimalMode: Boolean = false;
    var breakCommand: Boolean = false;
    var overflowFlag: Boolean = false;
    var negativeFlag: Boolean = false;

    fun reset() {
        this.x = Byte(0u)
        this.y = Byte(0u)
    }

    public fun storeX(x: Byte) {
        this.x = x
    }

    public fun storeY(y: Byte) {
        this.y = y
    }
}
