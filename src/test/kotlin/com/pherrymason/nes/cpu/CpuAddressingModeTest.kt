package com.pherrymason.nes.cpu

import com.pherrymason.nes.Address
import com.pherrymason.nes.NesByte
import com.pherrymason.nes.ProgramCounter
import com.pherrymason.nes.RAM
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class CpuAddressingModeTest {
    var ram = RAM()
    var cpu = CPU6502(ram)

    init {
        // Pointer's table on Page Zero ----------------
        // @ 0x0064 + 0x0065 => 0x0150
        ram.write(Address(100), NesByte(0x50))
        ram.write(Address(101), NesByte(0x01))

        ram.write(Address(102), NesByte(0x52))
        ram.write(Address(103), NesByte(0x01))

        ram.write(Address(104), NesByte(0x53))
        ram.write(Address(105), NesByte(0x01))

        // Pointer's table out of Page Zero ------------
        ram.write(Address(246), NesByte(246))
        ram.write(Address(247), NesByte(0))


        // End of pointer's table ----------------------

        ram.write(Address(256), NesByte(0x77))
    }

    fun signedToUnsigned(signedValue: Int): NesByte {
        return NesByte(signedValue + 128);
    }

    @Test
    fun decodeImmediateMode() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.ADC, AddressingMode.Immediate)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x50))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(1), address.address)
    }

    @Test
    fun decodeAbsoluteMode() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.ADC, AddressingMode.Absolute)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x50))
        ram.write(Address(2), NesByte(0x10))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(0x1050), address.address)
    }

    @Test
    fun decodeZeroPage() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.ADC, AddressingMode.ZeroPage)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x50))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(0x50), address.address)
    }

    @Test
    fun decodeZeroPageX() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.ADC, AddressingMode.ZeroPageX)
        cpu.registers.storeX(NesByte(1))
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x50))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(0x51), address.address)
    }

    @Test
    fun decodeZeroPageXWithOverflow() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.ADC, AddressingMode.ZeroPageX)
        cpu.registers.storeX(NesByte(1))
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0xFF))
        val pc = ProgramCounter(0)

        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(0), address.address)
    }

    @Test
    fun decodeZeroPageY() {
        this.cpu.registers.storeY(NesByte(1))
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.LDX, AddressingMode.ZeroPageY)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x50))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(0x51), address.address)
    }

    @Test
    fun decodeZeroPageYWithOverflow() {
        var instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.LDX, AddressingMode.ZeroPageY)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0xFF))

        this.cpu.registers.storeY(NesByte(1))
        instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.LDX, AddressingMode.ZeroPageY)
        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(0), address.address)
    }

    @Test
    fun decodeAbsoluteXIndexed() {
        val instruction =
            InstructionDescription.fromOPCodeAddressingMode(OPCode.ADC, AddressingMode.AbsoluteXIndexed);
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x50))
        ram.write(Address(2), NesByte(0x10))
        this.cpu.registers.storeX(NesByte(1))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(0x1051), address.address)
    }

    @Test
    fun decodeAbsoluteYIndexed() {
        val instruction =
            InstructionDescription.fromOPCodeAddressingMode(OPCode.ADC, AddressingMode.AbsoluteYIndexed)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x50))
        ram.write(Address(2), NesByte(0x10))
        this.cpu.registers.storeY(NesByte(1))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(0x1051), address.address)
    }

    @Test
    fun decodeRelative() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.BCC, AddressingMode.Relative)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), signedToUnsigned(1))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(pc.plus(2), address.address)
    }

    @Test
    fun decodeRelativeNeutral() {
        // OPCode with relative 0
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.BCC, AddressingMode.Relative)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), signedToUnsigned(0))
        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(pc + 1, address.address)
    }

    @Test
    fun decodeRelativeNegative() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.BCC, AddressingMode.Relative)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), signedToUnsigned(-1))

        val pc = ProgramCounter(1)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(pc.minus(1), address.address)
    }

    @Test
    fun decodeIndirect() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.JMP, AddressingMode.Indirect)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(100))
        ram.write(Address(2), NesByte(0))

        val pc = ProgramCounter(0)
        val address = cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(0x0150), address.address)
    }

    @Test
    fun decodePreIndexedIndirect() {
        val instruction =
            InstructionDescription.fromOPCodeAddressingMode(OPCode.ADC, AddressingMode.PreIndexedIndirect)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(101))
        cpu.registers.storeX(NesByte(1))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(0x0152), address.address)
    }

    @Test
    fun decodePreIndexedIndirectOverflows() {
        val instruction =
            InstructionDescription.fromOPCodeAddressingMode(OPCode.ADC, AddressingMode.PreIndexedIndirect)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(246))
        cpu.registers.storeX(NesByte(110))   // 246 + 110 will effectively point to 100

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(0x0150), address.address)
    }

    @Test
    fun decodePostIndirectIndexed() {
        val instruction =
            InstructionDescription.fromOPCodeAddressingMode(OPCode.ADC, AddressingMode.PostIndexedIndirect)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(104))
        cpu.registers.storeY(NesByte(0))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc + 1, instruction)

        assertEquals(Address(0x0153), address.address)
    }

    @Test
    fun decodePostIndirectIndexedCrossingPage() {
        val instruction =
            InstructionDescription.fromOPCodeAddressingMode(OPCode.ADC, AddressingMode.PostIndexedIndirect)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(246))
        cpu.registers.storeY(NesByte(10))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc+1, instruction)

        assertEquals(Address(256), address.address)
    }
}