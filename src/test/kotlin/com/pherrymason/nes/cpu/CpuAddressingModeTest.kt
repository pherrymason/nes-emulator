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
    var cpu = Cpu(ram)

    init {
        // Fill cpu memory with data: 0-9 raw values
        for (i in 0..9) {
            ram.write(Address(i), Byte(10 + i))     // #10
        }

        // OPCode with absolute mode
        var instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.Immediate)
        ram.write(Address(50), Byte(instruction.opcode))
        ram.write(Address(51), Byte(0x50))
        ram.write(Address(52), Byte(0x10))

        // OPCode with ZeroPage
        instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPage)
        ram.write(Address(53), Byte(instruction.opcode))
        ram.write(Address(54), Byte(0x50))

        // OPCode with ZeroPageX
        instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPageX)
        ram.write(Address(55), Byte(instruction.opcode))
        ram.write(Address(56), Byte(0x50))

        // OPCode with AbsoluteXIndexed
        instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.AbsoluteXIndexed)
        ram.write(Address(57), Byte(instruction.opcode))
        ram.write(Address(58), Byte(0x50))
        ram.write(Address(59), Byte(0x10))

        // OPCode with AbsoluteYIndexed
        instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.AbsoluteYIndexed)
        ram.write(Address(60), Byte(instruction.opcode))
        ram.write(Address(61), Byte(0x50))
        ram.write(Address(62), Byte(0x10))
    }

    @Test
    fun decodeImmediateMode() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.Immediate)
        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(1), address)
    }

    @Test
    fun decodeAbsoluteMode() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.Absolute)
        val pc = ProgramCounter(50)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x1050), address)
    }

    @Test
    fun decodeZeroPage() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPage)
        val pc = ProgramCounter(0)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(11), address)
    }

    @Test
    fun decodeZeroPageX() {
        this.cpu.registers.storeX(Byte(1))
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPageX)
        val pc = ProgramCounter(55)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x51), address)
    }

    @Test
    fun decodeZeroPageXWithOverflow() {
        var instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPageX)
        ram.write(Address(53), Byte(instruction.opcode))
        ram.write(Address(54), Byte(0xFF))

        this.cpu.registers.storeX(Byte(1))
        instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.ZeroPageX)
        val pc = ProgramCounter(53)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0), address)
    }

    @Test
    fun decodeZeroPageY() {
        this.cpu.registers.storeY(Byte(1))
        val instruction = Instruction.fromInstructionCode(InstructionCode.LDX, AddressingMode.ZeroPageY)
        val pc = ProgramCounter(55)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x51), address)
    }

    @Test
    fun decodeZeroPageYWithOverflow() {
        var instruction = Instruction.fromInstructionCode(InstructionCode.LDX, AddressingMode.ZeroPageY)
        ram.write(Address(53), Byte(instruction.opcode))
        ram.write(Address(54), Byte(0xFF))

        this.cpu.registers.storeY(Byte(1))
        instruction = Instruction.fromInstructionCode(InstructionCode.LDX, AddressingMode.ZeroPageY)
        val pc = ProgramCounter(53)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0), address)
    }

    @Test
    fun decodeAbsoluteXIndexed() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.AbsoluteXIndexed);
        this.cpu.registers.storeX(Byte(1))

        val pc = ProgramCounter(57)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x1051), address)
    }

    @Test
    fun decodeAbsoluteYIndexed() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.ADC, AddressingMode.AbsoluteYIndexed);
        this.cpu.registers.storeY(Byte(1))

        val pc = ProgramCounter(60)
        val address = this.cpu.decodeOperationAddress(pc, instruction)

        assertEquals(Address(0x1051), address)
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