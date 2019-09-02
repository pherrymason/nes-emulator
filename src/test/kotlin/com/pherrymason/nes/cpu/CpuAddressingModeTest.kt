package com.pherrymason.nes.cpu

import com.pherrymason.nes.Address
import com.pherrymason.nes.Byte
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
        ram.write(Address(100), Byte(0x50))
        ram.write(Address(101), Byte(0x01))

        ram.write(Address(102), Byte(0x52))
        ram.write(Address(103), Byte(0x01))

        ram.write(Address(104), Byte(0x53))
        ram.write(Address(105), Byte(0x01))
        // End of pointer's table ----------------------
    }

    fun signedToUnsigned(signedValue: Int): Byte {
        return Byte(signedValue + 128);
    }

    @Test
    fun decodeImmediateMode() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.Immediate)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), Byte(0x50))
        ram.write(Address(2), Byte(0x10))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(1), address)
    }

    @Test
    fun decodeAbsoluteMode() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.Absolute)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), Byte(0x50))
        ram.write(Address(2), Byte(0x10))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x1050), address)
    }

    @Test
    fun decodeZeroPage() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPage)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), Byte(0x50))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x50), address)
    }

    @Test
    fun decodeZeroPageX() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPageX)
        cpu.registers.storeX(Byte(1))
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), Byte(0x50))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x51), address)
    }

    @Test
    fun decodeZeroPageXWithOverflow() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPageX)
        cpu.registers.storeX(Byte(1))
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), Byte(0xFF))
        val pc = ProgramCounter(0)

        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0), address)
    }

    @Test
    fun decodeZeroPageY() {
        this.cpu.registers.storeY(Byte(1))
        val instruction = Instruction.fromInstructionCode(InstructionCode.LDX, AddressingMode.ZeroPageY)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), Byte(0x50))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x51), address)
    }

    @Test
    fun decodeZeroPageYWithOverflow() {
        var instruction = Instruction.fromInstructionCode(InstructionCode.LDX, AddressingMode.ZeroPageY)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), Byte(0xFF))

        this.cpu.registers.storeY(Byte(1))
        instruction = Instruction.fromInstructionCode(InstructionCode.LDX, AddressingMode.ZeroPageY)
        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0), address)
    }

    @Test
    fun decodeAbsoluteXIndexed() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.AbsoluteXIndexed);
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), Byte(0x50))
        ram.write(Address(2), Byte(0x10))
        this.cpu.registers.storeX(Byte(1))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x1051), address)
    }

    @Test
    fun decodeAbsoluteYIndexed() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.AbsoluteYIndexed)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), Byte(0x50))
        ram.write(Address(2), Byte(0x10))
        this.cpu.registers.storeY(Byte(1))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x1051), address)
    }

    @Test
    fun decodeRelative() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.BCC, AddressingMode.Relative)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), signedToUnsigned(1))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(pc.plus(1), address)
    }

    @Test
    fun decodeRelativeNeutral() {
        // OPCode with relative 0
        val instruction = Instruction.fromInstructionCode(InstructionCode.BCC, AddressingMode.Relative)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), signedToUnsigned(0))
        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(pc, address)
    }

    @Test
    fun decodeRelativeNegative() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.BCC, AddressingMode.Relative)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), signedToUnsigned(-1))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(pc.minus(1), address)
    }

    @Test
    fun decodeIndirect() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.JMP, AddressingMode.Indirect)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), Byte(100))
        ram.write(Address(2), Byte(0))

        val pc = ProgramCounter(0)
        val address = cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x0150), address)
    }

    @Test
    fun decodePreIndexedIndirect() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.PreIndexedIndirect)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), Byte(101))
        cpu.registers.storeX(Byte(1))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x0152), address)
    }

    @Test
    fun decodePostIndirectIndexed() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.PostIndexedIndirect)
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), Byte(104))
        ram.write(Address(2), Byte(0))
        cpu.registers.storeY(Byte(0))

        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x0153), address)
    }
/*
    @Test
    fun decodeZeroPageXIndexed() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPageX)
        this.cpu.registers.storeX(Byte(1))
        val value = this.cpu.decodeOperationAddress(Address(0), instruction)

        // value at @1 = 11
        // 11 + X = 12
        // value at @12 = 0x03
        assertEquals(value, ram.read(12))
    }

    @Test
    fun decodeZeroPageXIndexedOverflows() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPageX)
        this.cpu.registers.storeX(Byte(127))
        val value = this.cpu.decodeOperationAddress(Address(100), instruction)

        // value at @101 = 255
        // 255 + X = 510 => 0xFFE -> 0xFE
        // value at @FE(254) = 0x66
        assertEquals(value, ram.read(254))
    }
    @Test
    fun decodeZeroPageYIndexed() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPageY)
        this.cpu.registers.storeY(Byte(1))
        val value = this.cpu.decodeOperationAddress(Address(0), instruction)

        // value at @1 = 11
        // 11 + X = 12
        // value at @12 = 0x03
        assertEquals(value, ram.read(12))
    }

    @Test
    fun decodeZeroPageYIndexedOverflows() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPageY)
        this.cpu.registers.storeY(Byte(127))
        val value = this.cpu.decodeOperationAddress(Address(100), instruction)

        // value at @101 = 255
        // 255 + X = 510 => 0xFFE -> 0xFE
        // value at @FE(254) = 0x66
        assertEquals(value, ram.read(254), "Evaluated address does not equals to ram[254]")
    }*/
}