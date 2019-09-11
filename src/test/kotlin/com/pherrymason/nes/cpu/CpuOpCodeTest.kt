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
        val instruction = InstructionDescription.fromInstructionCode(InstructionCode.AND, AddressingMode.Immediate)
        ram.write(Address(0), instruction.opcode)

        // --------------------------------------------------
        // Case 1
        ram.write(Address(1), NesByte(0x8F))
        cpu.registers.a = NesByte(0x8F)
        cpu.clock()

        assertEquals(cpu.registers.ps.zeroFlag, false)
        assertEquals(cpu.registers.ps.negativeFlag, true)
        assertEquals(cpu.registers.a.byte, NesByte(0x8F).byte, "acumulator failed")

        // --------------------------------------------------
        // Case 2: Zero flag properly set
        ram.write(Address(1), NesByte(0x8F))
        cpu.registers.a = NesByte(0x70)
        cpu.clock()

        assertEquals(cpu.registers.ps.zeroFlag, true)
        assertEquals(cpu.registers.ps.negativeFlag, false)
        assertEquals(cpu.registers.a.byte, NesByte(0x00).byte)

        // --------------------------------------------------
        // Case 3: Negative flag properly set
        ram.write(Address(1), NesByte(0x8F))
        cpu.registers.a = NesByte(0x8F)
        cpu.clock()

        assertEquals(cpu.registers.ps.zeroFlag, false)
        assertEquals(cpu.registers.ps.negativeFlag, true)
        assertEquals(cpu.registers.a.byte, NesByte(0x8F).byte)
    }

    @Test
    fun BRKTest() {
        val instruction = InstructionDescription.fromInstructionCode(InstructionCode.BRK, AddressingMode.Implied)

        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x00))
        // Setup an address in vector 0xFFFE-0xFFFF
        ram.write(Address(0xFFFE), NesByte(0x03))
        ram.write(Address(0xFFFF), NesByte(0xFF))


        cpu.clock()
        assertEquals(cpu.registers.pc, Address(0xFF03), "PC does not point correctly")

        // The stack should contain the value of PC
        val loByte = ram.read(ram.STACK_ADDRESS + 0xFF)
        val hiByte = ram.read(ram.STACK_ADDRESS + 0xFE)

        assertEquals(Address(loByte, hiByte), Address(0x02), "Stack does not contain expected value")

        // The stack should contain the value of Processor Status
        val psDump = ram.read(ram.STACK_ADDRESS + 0xFD)
        assertEquals(cpu.registers.ps.dump(), psDump, "Processor status was not copied to the Stack")

        assertEquals(true, cpu.registers.ps.interruptDisabled, "Interrupt flag is not set")
        assertEquals(true, cpu.registers.ps.breakCommand, "Break flag is not set")
    }
}