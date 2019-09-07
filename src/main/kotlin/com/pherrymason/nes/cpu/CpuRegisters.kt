package com.pherrymason.nes.cpu

import com.pherrymason.nes.NesByte
import com.pherrymason.nes.Word



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
    // can be accessed using interrupts, pulls, pushes, and transfers.
    var s: NesByte = NesByte(0x00);

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
    var ps = NesByte(0x00)
    enum class Flag{
        C, // Carry Bit             0
        Z, // Zero Flag             1
        I, // Interrupt Disable     2
        D, // Decimal mode          3
        B, // Break command         4
        U, // Unused                5
        V, // Overflow              6
        N, // Negative              7
    }

    fun reset() {
        this.pc = Word(0)
        this.x = NesByte(0)
        this.y = NesByte(0)
    }

    fun storeX(x: NesByte) {
        this.x = x
    }

    fun storeY(y: NesByte) {
        this.y = y
    }

    fun setFlag(flag: Flag, bit: Boolean) {
        var shift = 0;
        when (flag){
            Flag.C -> shift = 0
            Flag.Z -> shift = 1
            Flag.I -> shift = 2
            Flag.D -> shift = 3
            Flag.B -> shift = 4
            Flag.U -> shift = 5
            Flag.V -> shift = 6
            Flag.N -> shift = 7
        }

        val bitValue = NesByte(1).shl(shift)
        if (bit) {
            ps = ps.or(bitValue)
        } else {
            ps = ps.and(bitValue.inv())
        }
    }

    fun getFlag(flag: Flag): Boolean {
        var shift = 0;
        when (flag){
            Flag.C -> shift = 0
            Flag.Z -> shift = 1
            Flag.I -> shift = 2
            Flag.D -> shift = 3
            Flag.B -> shift = 4
            Flag.U -> shift = 5
            Flag.V -> shift = 6
            Flag.N -> shift = 7
        }

        val bitValue = ps.shr(shift).and(NesByte(0x01))

        return bitValue.toBool()
    }

    fun setNegativeFlag(value: NesByte) {
        val flag = (value and NesByte(0x80)).toBool()
        setFlag(Flag.N, flag)
        //this.negativeFlag = flag
    }

    fun setZeroFlag(value: NesByte) {
        val result = value == NesByte(0)
        //this.zeroFlag = result
        setFlag(Flag.Z, result)
    }
}
