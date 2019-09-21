package com.pherrymason.nes.cpu

import com.pherrymason.nes.NesByte
import com.pherrymason.nes.Word

@ExperimentalUnsignedTypes
class ProcessorStatus {
    var carryBit: Boolean = false
    var zeroFlag: Boolean = false
    var interruptDisabled: Boolean = false
    var decimalMode: Boolean = false
    var breakCommand: Boolean = false
    var unusedFlag: Boolean = false
    var overflowFlag: Boolean = false
    var negativeFlag: Boolean = false

    enum class Flag(val shift: Int){
        C(0), // Carry Bit
        Z(1), // Zero Flag
        I(2), // Interrupt Disable
        D(3), // Decimal mode
        B(4), // Break command. Documentation states no actual B flag exists in Status regsiter.
                    // This flag only exist when the Status is dumped into the stack.
        U(5), // Unused
        V(6), // Overflow
        N(7), // Negative
    }

    fun updateNegativeFlag(value: NesByte) {
        this.negativeFlag = value and 0x80 == NesByte(0x80)
    }

    fun updateZeroFlag(value: NesByte) {
        this.zeroFlag = (value == NesByte(0))
    }

    fun dump(): NesByte {
        var ps = NesByte(0)

        ps = setFlag(Flag.C, carryBit, ps)
        ps = setFlag(Flag.Z, zeroFlag, ps)
        ps = setFlag(Flag.I, interruptDisabled, ps)
        ps = setFlag(Flag.D, decimalMode, ps)
        ps = setFlag(Flag.B, breakCommand, ps)
        ps = setFlag(Flag.U, unusedFlag, ps)
        ps = setFlag(Flag.V, overflowFlag, ps)
        ps = setFlag(Flag.N, negativeFlag, ps)

        return ps
    }

    fun setFlag(flag: Flag, bit: Boolean, byte: NesByte): NesByte {
        val bitValue = NesByte(1).shl(flag.shift)
        if (bit) {
            return byte.or(bitValue)
        } else {
            return byte.and(bitValue.inv())
        }
    }
}

@ExperimentalUnsignedTypes
class CpuRegisters {
    // Accumulator
    // and along with the arithmetic logic unit (ALU), supports using the status register for carrying, overflow
    // detection, and so on.
    var a: NesByte = NesByte(0x00);

    // Indexes X Y
    // used for several addressing modes. They can be used as loop counters easily, using INC/DEC and branch
    // instructions. Not being the accumulator, they have limited addressing modes themselves when loading and saving.
    var x: NesByte = NesByte(0x00);
    var y: NesByte = NesByte(0x00);

    // Program Counter:
    // supports 65536 direct (unbanked) memory locations, however not all values are sent to the cartridge.
    // It can be accessed either by allowing CPU's internal fetch logic increment the address bus, an interrupt
    // (NMI, Reset, IRQ/BRQ), and using the RTS/JMP/JSR/Branch instructions.
    var pc: Word = Word(0x0000);

    // Stack Pointer:
    // The NMOS 65xx processors have 256 bytes of stack memory, ranging
    // from $0100 to $01FF. The S register is a 8-bit offset to the stack
    // page. In other words, whenever anything is being pushed on the
    // stack, it will be stored to the address $0100+S.
    //
    // The Stack pointer can be read and written by transfering its value
    // to or from the index register X (see below) with the TSX and TXS
    // this register is decremented every time a byte is pushed onto the stack,
    // and incremented when a byte is popped off the stack.
    var sp: NesByte = NesByte(0xFF);

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
    var ps = ProcessorStatus()

    fun reset() {
        this.pc = Word(0)
        this.ps = ProcessorStatus()
        this.sp = NesByte(0xFF)
        this.x = NesByte(0)
        this.y = NesByte(0)
    }

    fun storeX(x: NesByte) {
        this.x = x
    }

    fun storeY(y: NesByte) {
        this.y = y
    }
}
