package com.pherrymason.nes.cpu

import com.pherrymason.nes.Address
import com.pherrymason.nes.RAM
import com.pherrymason.nes.NesByte
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class CpuOpCodeTest {
    var ram = RAM()
    var cpu = CPU6502(ram)

    @Test
    fun ANDTest() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.AND, AddressingMode.Immediate)
        ram.write(Address(0), instruction.opcode)

        // --------------------------------------------------
        // Case 1
        ram.write(Address(1), NesByte(0x8F))
        cpu.registers.a = NesByte(0x8F)
        cpu.clock()

        assertEquals(cpu.registers.getFlag(CpuRegisters.Flag.Z), false)
        assertEquals(cpu.registers.getFlag(CpuRegisters.Flag.N), true)
        assertEquals(cpu.registers.a.byte, NesByte(0x8F).byte, "acumulator failed")

        // --------------------------------------------------
        // Case 2: Zero flag properly set
        ram.write(Address(1), NesByte(0x8F))
        cpu.registers.a = NesByte(0x70)
        cpu.clock()

        assertEquals(cpu.registers.getFlag(CpuRegisters.Flag.Z), true)
        assertEquals(cpu.registers.getFlag(CpuRegisters.Flag.N), false)
        assertEquals(cpu.registers.a.byte, NesByte(0x00).byte)

        // --------------------------------------------------
        // Case 3: Negative flag properly set
        ram.write(Address(1), NesByte(0x8F))
        cpu.registers.a = NesByte(0x8F)
        cpu.clock()

        assertEquals(cpu.registers.getFlag(CpuRegisters.Flag.Z), false)
        assertEquals(cpu.registers.getFlag(CpuRegisters.Flag.N), true)
        assertEquals(cpu.registers.a.byte, NesByte(0x8F).byte)
    }

    @Test
    fun BRKTest() {
        val instruction = Instruction.fromInstructionCode(InstructionCode.BRK, AddressingMode.Implied)

        ram.write(Address(0), instruction.opcode)
        cpu.clock()
    }
}