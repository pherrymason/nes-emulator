package com.pherrymason.nes.cpu

import com.pherrymason.nes.Address
import com.pherrymason.nes.RAM
import com.pherrymason.nes.NesByte
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class CpuOpCodeTest {
    var ram = RAM()
    var cpu = CPU6502(ram)

    @Test
    fun ANDTest() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.AND, AddressingMode.Immediate)
        ram.write(Address(0), instruction.opcode)

        // --------------------------------------------------
        // Case 1
        ram.write(Address(1), NesByte(0x8F))
        cpu.registers.a = NesByte(0x8F)
        cpu.clock()

        assertEquals(cpu.registers.ps.zeroFlag, false)
        assertEquals(cpu.registers.ps.negativeFlag, true)
        assertEquals(cpu.registers.a.byte, NesByte(0x8F).byte, "accumulator failed")

        // --------------------------------------------------
        // Case 2: Zero flag properly set
        cpu.reset()
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x8F))
        cpu.registers.a = NesByte(0x70)
        cpu.clock()

        assertTrue(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        assertEquals(cpu.registers.a.byte, NesByte(0x00).byte)

        // --------------------------------------------------
        // Case 3: Negative flag properly set
        cpu.reset()
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x8F))
        cpu.registers.a = NesByte(0x8F)
        cpu.clock()

        assertFalse(cpu.registers.ps.zeroFlag)
        assertTrue(cpu.registers.ps.negativeFlag)
        assertEquals(cpu.registers.a.byte, NesByte(0x8F).byte)
    }

    @Test
    fun BRKTest() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.BRK, AddressingMode.Implied)

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
        val expectedDump = NesByte(0x14)
        assertEquals(expectedDump, psDump, "Processor status was not copied to the Stack")

        assertEquals(true, cpu.registers.ps.interruptDisabled, "Interrupt flag is not set")
        assertEquals(false, cpu.registers.ps.breakCommand, "Break flag is not set")
    }

    @Test
    fun BCCTest() {
        // If the carry flag is clear then add the relative displacement to the
        // program counter to cause a branch to a new location
        // This scenario should jump to 0x010 if carry flag.
        // else it should jump to 0x02
        val instruction = InstructionDescription.fromOPCodeAddressingMode(
            OPCode.BCC, AddressingMode
                .Relative
        )

        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))

        // Scenario 1: No carry flag
        cpu.registers.ps.carryBit = true
        cpu.clock()
        assertEquals(Address(0x02), cpu.registers.pc)

        // Scenario 2: Carry flag is set
        cpu.reset()
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.clock()
        assertEquals(Address(10), cpu.registers.pc)

        // Scenario 3: TODO Memory page trespassed causes +1 cycle
    }

    @Test
    fun BCSTest() {
        // If the carry flag is set then add the relative displacement to the
        // program counter to cause a branch to a new location
        // This scenario should jump to 0x010 if carry flag.
        // else it should jump to 0x02
        val instruction = InstructionDescription.fromOPCodeAddressingMode(
            OPCode.BCS, AddressingMode
                .Relative
        )

        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))

        // Scenario 1: No carry flag
        cpu.clock()
        assertEquals(Address(0x02), cpu.registers.pc)

        // Scenario 2: Carry flag is set
        cpu.reset()
        cpu.registers.ps.carryBit = true
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.clock()
        assertEquals(Address(10), cpu.registers.pc)

        // Scenario 3: TODO Memory page trespassed causes +1 cycle
    }

    @Test
    fun BEQTest() {
        // If the zero flag is set then add the relative displacement to the program counter
        // to cause a branch to a new location
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.BEQ, AddressingMode.Relative)

        // Scenario 1: Zero flag is set
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.registers.ps.zeroFlag = true
        cpu.clock()
        assertEquals(Address(10), cpu.registers.pc)

        // Scenario 2: Zero flag is not set
        cpu.reset()
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.clock()
        assertEquals(Address(2), cpu.registers.pc)
    }

    @Test
    fun BNETest() {
        // If the zero flag is clear then add the relative displacement to the program
        // counter to cause a branch to a new location.
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.BNE, AddressingMode.Relative)

        // Scenario 1: Zero flag is clear
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.registers.ps.zeroFlag = false
        cpu.clock()
        assertEquals(Address(10), cpu.registers.pc)

        // Scenario 2: Zero flag is set
        cpu.reset()
        cpu.registers.ps.zeroFlag = true
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.clock()
        assertEquals(Address(2), cpu.registers.pc)
    }

    @Test
    fun BPLTest() {
        // If the negative flag is clear then add the relative displacement to the
        // program counter to cause a branch to a new location.
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.BPL, AddressingMode.Relative)

        // Scenario 1: Zero flag is clear
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.registers.ps.negativeFlag = false
        cpu.clock()
        assertEquals(Address(10), cpu.registers.pc)

        // Scenario 2: Zero flag is set
        cpu.reset()
        cpu.registers.ps.negativeFlag = true
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.clock()
        assertEquals(Address(2), cpu.registers.pc)
    }

    @Test
    fun BMITest() {
        // If the negative flag is set then add the relative displacement to the program counter to cause a branch to a
        // new location.
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.BMI, AddressingMode.Relative)

        // Scenario 1: Zero flag is set
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.registers.ps.negativeFlag = true
        cpu.clock()
        assertEquals(Address(10), cpu.registers.pc)

        // Scenario 2: Zero flag is not set
        cpu.reset()
        cpu.registers.ps.negativeFlag = false
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.clock()
        assertEquals(Address(2), cpu.registers.pc)
    }

    @Test
    fun BVCTest() {
        // If the negative flag is set then add the relative displacement to the program counter to cause a branch to a
        // new location.
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.BVC, AddressingMode.Relative)

        // Scenario 1: Zero flag is set
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.registers.ps.overflowFlag = false
        cpu.clock()
        assertEquals(Address(10), cpu.registers.pc)

        // Scenario 2: Zero flag is not set
        cpu.reset()
        cpu.registers.ps.overflowFlag = true
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.clock()
        assertEquals(Address(2), cpu.registers.pc)
    }

    @Test
    fun BVSTest() {
        // If the negative flag is set then add the relative displacement to the program counter to cause a branch to a
        // new location.
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.BVS, AddressingMode.Relative)

        // Scenario 1: Zero flag is set
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.registers.ps.overflowFlag = true
        cpu.clock()
        assertEquals(Address(10), cpu.registers.pc)

        // Scenario 2: Zero flag is not set
        cpu.reset()
        cpu.registers.ps.overflowFlag = false
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(8 + 128))
        cpu.clock()
        assertEquals(Address(2), cpu.registers.pc)
    }

    @Test
    fun CLCTest() {
        // Set the carry flag to zero.
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.CLC, AddressingMode.Implied)

        ram.write(Address(0), instruction.opcode)
        cpu.registers.ps.carryBit = true
        cpu.clock()

        assertEquals(false, cpu.registers.ps.carryBit)
    }

    @Test
    fun CLDTest() {
        // Set the decimal mode flag to zero.
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.CLD, AddressingMode.Implied)

        ram.write(Address(0), instruction.opcode)
        cpu.registers.ps.decimalMode = true
        cpu.clock()

        assertEquals(false, cpu.registers.ps.decimalMode)
    }

    @Test
    fun CLITest() {
        // Set the interrupt flag to zero.
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.CLI, AddressingMode.Implied)

        ram.write(Address(0), instruction.opcode)
        cpu.registers.ps.interruptDisabled = true
        cpu.clock()

        assertEquals(false, cpu.registers.ps.interruptDisabled)
    }

    @Test
    fun CLVTest() {
        // Set the overflow flag to zero.
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.CLV, AddressingMode.Implied)

        ram.write(Address(0), instruction.opcode)
        cpu.registers.ps.overflowFlag = true
        cpu.clock()

        assertEquals(false, cpu.registers.ps.overflowFlag)
    }

    @Test
    fun CMPTest() {
        // This instruction compares the contents of the accumulator with another memory held value
        // and sets the zero and carry flags as appropriate.
        // C 	Carry Flag 	Set if A >= M
        //Z 	Zero Flag 	Set if A = M
        //I 	Interrupt Disable 	Not affected
        //D 	Decimal Mode Flag 	Not affected
        //B 	Break Command 	Not affected
        //V 	Overflow Flag 	Not affected
        //N 	Negative Flag 	Set if bit 7 of the result is set

        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.CMP, AddressingMode.Immediate)

        // Scenario 1: M <= A
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x00))
        cpu.registers.a = NesByte(0x01)
        cpu.clock()
        assertEquals(true, cpu.registers.ps.carryBit)
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 2: M == A
        cpu.reset()
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x01))
        cpu.registers.a = NesByte(0x01)
        cpu.clock()
        assertEquals(true, cpu.registers.ps.carryBit)
        assertEquals(true, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 3: M > A
        cpu.reset()
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x02))
        cpu.registers.a = NesByte(0x01)
        cpu.clock()
        assertEquals(false, cpu.registers.ps.carryBit)
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(true, cpu.registers.ps.negativeFlag)
    }

    @Test
    fun CPXTest() {
        // This instruction compares the contents of the X register with another memory held value and
        // sets the zero and carry flags as appropriate.
        // Z,C,N = X-M
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.CPX, AddressingMode.Immediate)

        // Scenario 1: X >= M
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x01))
        cpu.registers.x = NesByte(0x10)
        cpu.clock()
        assertEquals(true, cpu.registers.ps.carryBit)
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 2: X == M
        cpu.reset()
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x10))
        cpu.registers.x = NesByte(0x10)
        cpu.clock()
        assertEquals(true, cpu.registers.ps.carryBit)
        assertEquals(true, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 3: X-M < 0
        cpu.reset()
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x12))
        cpu.registers.x = NesByte(0x10)
        cpu.clock()
        assertEquals(false, cpu.registers.ps.carryBit)
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(true, cpu.registers.ps.negativeFlag)
    }

    @Test
    fun CPYTest() {
        // This instruction compares the contents of the Y register with another memory held value and
        // sets the zero and carry flags as appropriate.
        // Z,C,N = Y-M
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.CPY, AddressingMode.Immediate)

        // Scenario 1: Y >= M
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x01))
        cpu.registers.y = NesByte(0x10)
        cpu.clock()
        assertEquals(true, cpu.registers.ps.carryBit)
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 2: Y == M
        cpu.reset()
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x10))
        cpu.registers.y = NesByte(0x10)
        cpu.clock()
        assertEquals(true, cpu.registers.ps.carryBit)
        assertEquals(true, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 3: Y-M < 0
        cpu.reset()
        ram.write(Address(0), instruction.opcode)
        ram.write(Address(1), NesByte(0x12))
        cpu.registers.y = NesByte(0x10)
        cpu.clock()
        assertEquals(false, cpu.registers.ps.carryBit)
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(true, cpu.registers.ps.negativeFlag)
    }

    @Test
    fun DECTest() {
        // Subtracts one from the value held at a specified memory location setting the zero and
        // negative flags as appropriate.
        // M,Z,N = M-1
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.DEC, AddressingMode.ZeroPage)

        // Scenario 1: Result is not zero nor negative
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, NesByte(0xFF))
        ram.write(Address(0xFF), NesByte(10))
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(9), ram.read(Address(0xFF)))
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 2: Result is zero
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, NesByte(0xFF))
        ram.write(Address(0xFF), NesByte(1))
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(0), ram.read(Address(0xFF)))
        assertEquals(true, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 3: Result is negative
        cpu.reset()
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, NesByte(0xFF))
        ram.write(Address(0xFF), NesByte(0))
        cpu.clock()
        assertEquals(NesByte(255), ram.read(Address(0xFF)))
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(true, cpu.registers.ps.negativeFlag)
    }

    @Test
    fun DEXTest() {
        // Subtracts one from the X register setting the zero and negative flags as appropriate.
        // X,Z,N = X-1
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.DEX, AddressingMode.Implied)

        // Scenario 1: Result is not zero nor negative
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.x = NesByte(10)
        cpu.clock()
        assertEquals(NesByte(9), cpu.registers.x)
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 2: Result is zero
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.x = NesByte(1)
        cpu.clock()
        assertEquals(NesByte(0), cpu.registers.x)
        assertEquals(true, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 3: Result is negative
        cpu.reset()
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.x = NesByte(0)
        cpu.clock()
        assertEquals(NesByte(255), cpu.registers.x)
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(true, cpu.registers.ps.negativeFlag)
    }

    @Test
    fun DEYTest() {
        // Subtracts one from the Y register setting the zero and negative flags as appropriate.
        // Y,Z,N = Y-1
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.DEY, AddressingMode.Implied)

        // Scenario 1: Result is not zero nor negative
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.y = NesByte(10)
        cpu.clock()
        assertEquals(NesByte(9), cpu.registers.y)
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 2: Result is zero
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.y = NesByte(1)
        cpu.clock()
        assertEquals(NesByte(0), cpu.registers.y)
        assertEquals(true, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 3: Result is negative
        cpu.reset()
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.y = NesByte(0)
        cpu.clock()
        assertEquals(NesByte(255), cpu.registers.y)
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(true, cpu.registers.ps.negativeFlag)
    }

    @Test
    fun INCTest() {
        // Adds one to the value held at a specified memory location setting the zero and
        // negative flags as appropriate.
        // M,Z,N = M+1
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.INC, AddressingMode.ZeroPage)

        // Scenario 1: Result is not zero nor negative
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, NesByte(0xFF))
        ram.write(Address(255), NesByte(8))
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(9), ram.read(Address(255)))
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 2: Result is zero
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, NesByte(0xFF))
        ram.write(Address(255), NesByte(255))
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(0), ram.read(Address(255)))
        assertTrue(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)

        // Scenario 3: Result is negative
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, NesByte(255))
        ram.write(Address(255), NesByte(0x7F))
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(0x80), ram.read(Address(255)))
        assertFalse(cpu.registers.ps.zeroFlag)
        assertTrue(cpu.registers.ps.negativeFlag)
    }

    @Test
    fun INXTest() {
        // Adds one to the X register setting the zero and negative flags as appropriate.
        // X,Z,N = X+1
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.INX, AddressingMode.Implied)

        // Scenario 1: Result is not zero nor negative
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.x = NesByte(8)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(9), cpu.registers.x)
        assertEquals(false, cpu.registers.ps.zeroFlag)
        assertEquals(false, cpu.registers.ps.negativeFlag)

        // Scenario 2: Result is zero
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.x = NesByte(0xFF)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(0), cpu.registers.x)
        assertTrue(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)

        // Scenario 3: Result is negative
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.x = NesByte(0x7F)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(0x80), cpu.registers.x)
        assertFalse(cpu.registers.ps.zeroFlag)
        assertTrue(cpu.registers.ps.negativeFlag)
    }

    @Test
    fun INYTest() {
        // Adds one to the Y register setting the zero and negative flags as appropriate.
        // Y,Z,N = Y+1
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.INY, AddressingMode.Implied)

        // Scenario 1: Result is not zero nor negative
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.y = NesByte(8)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(9), cpu.registers.y)
        assertFalse(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)

        // Scenario 2: Result is zero
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.y = NesByte(0xFF)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(0), cpu.registers.y)
        assertTrue(cpu.registers.ps.zeroFlag)
        assertFalse( cpu.registers.ps.negativeFlag)

        // Scenario 3: Result is negative
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.y = NesByte(0x7F)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(0x80), cpu.registers.y)
        assertFalse(cpu.registers.ps.zeroFlag)
        assertTrue( cpu.registers.ps.negativeFlag)
    }

    @Test
    fun JMPTest() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.JMP, AddressingMode.Absolute)

        // Scenario 1
        val jumpTo = Address(0x200)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, jumpTo.lowByte())
        ram.write(ram.PROGRAM_ADDRESS + 2, jumpTo.highByte())
        cpu.registers.pc = ram.PROGRAM_ADDRESS;
        cpu.clock()

        assertEquals(jumpTo, cpu.registers.pc)
    }

    @Test
    fun JSRTest() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.JSR, AddressingMode.Absolute)

        // Scenario 1
        val jumpTo = Address(0x200)
        cpu.reset()
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, jumpTo.lowByte())
        ram.write(ram.PROGRAM_ADDRESS + 2, jumpTo.highByte())
        cpu.registers.pc = ram.PROGRAM_ADDRESS;
        cpu.clock()

        assertEquals(jumpTo, cpu.registers.pc)

        val addressAtStack = Address(
            cpu.read(ram.STACK_ADDRESS + 0xFE),
            cpu.read(ram.STACK_ADDRESS + 0xFF)
        )
        assertEquals(ram.PROGRAM_ADDRESS, addressAtStack)
    }

    @Test
    fun LDATest() {
        // Z: Set if A = 0
        // N: set if bit 7 of A is set
        var value = NesByte(0x01)
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.LDA, AddressingMode.Immediate)

        // Scenario 1: Immediate
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, value)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(value, cpu.registers.a, "Wrong register A value in addressing mode $instruction")
        assertFalse(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        cpu.reset()

        // Scenario 2: value is 0
        value = NesByte(0x00)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, value)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(value, cpu.registers.a, "Wrong register A value in addressing mode $instruction")
        assertTrue(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        cpu.reset()

        // Scenario 3: value is negative
        value = NesByte(0x81)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, value)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(value, cpu.registers.a, "Wrong register A value in addressing mode $instruction")
        assertFalse(cpu.registers.ps.zeroFlag)
        assertTrue(cpu.registers.ps.negativeFlag)
        cpu.reset()
    }

    @Test
    fun LDXTest() {
        // Z: Set if X = 0
        // N: set if bit 7 of X is set
        var value = NesByte(0x01)
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.LDX, AddressingMode.Immediate)

        // Scenario 1: Immediate
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, value)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(value, cpu.registers.x, "Wrong register X value")
        assertFalse(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        cpu.reset()

        // Scenario 2: value is 0
        value = NesByte(0x00)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, value)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(value, cpu.registers.x, "Wrong register X value")
        assertTrue(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        cpu.reset()

        // Scenario 3: value is negative
        value = NesByte(0x81)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, value)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(value, cpu.registers.x, "Wrong register X value")
        assertFalse(cpu.registers.ps.zeroFlag)
        assertTrue(cpu.registers.ps.negativeFlag)
        cpu.reset()
    }

    @Test
    fun LDYTest() {
        // Z: Set if Y = 0
        // N: set if bit 7 of Y is set
        var value = NesByte(0x01)
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.LDY, AddressingMode.Immediate)

        // Scenario 1: Immediate
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, value)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(value, cpu.registers.y, "Wrong register Y value")
        assertFalse(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        cpu.reset()

        // Scenario 2: value is 0
        value = NesByte(0x00)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, value)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(value, cpu.registers.y, "Wrong register Y value")
        assertTrue(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        cpu.reset()

        // Scenario 3: value is negative
        value = NesByte(0x81)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, value)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(value, cpu.registers.y, "Wrong register Y value")
        assertFalse(cpu.registers.ps.zeroFlag)
        assertTrue(cpu.registers.ps.negativeFlag)
        cpu.reset()
    }


    @Test
    fun LSRTest() {
        // Logical Shift Right
        // A,C,Z,N = A/2 or M,C,Z,N = M/2
        // Each of the bits in A or M is shift one place to the right.
        // The bit that was in bit 0 is shifted into the carry flag.
        // Bit 7 is set to zero.

        // Scenario 1: default
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.LSR, AddressingMode.Accumulator)
        cpu.registers.a = NesByte(0b01010101)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(NesByte(0b00101010), cpu.registers.a)
        assertTrue(cpu.registers.ps.carryBit)
        assertFalse(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        cpu.reset()

        // Scenario 2: result is 0
        cpu.registers.a = NesByte(0b00000001)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(NesByte(0b00000000), cpu.registers.a)
        assertTrue(cpu.registers.ps.carryBit)
        assertTrue(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        cpu.reset()
    }

    @Test
    fun ORATest() {

        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.ORA, AddressingMode.Immediate)


        // Scenario 1: default
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, NesByte(0b00101010))
        cpu.registers.a = NesByte(0b01010101)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(0b01111111), cpu.registers.a)
        assertFalse(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)

        // Scenario 2: result is 0
        cpu.reset()
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, NesByte(0b00000000))
        cpu.registers.a = NesByte(0b00000000)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(0x00), cpu.registers.a)
        assertTrue(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)

        // Scenario 3: result is negative
        cpu.reset()
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.PROGRAM_ADDRESS + 1, NesByte(0b10101010))
        cpu.registers.a = NesByte(0b01010101)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()
        assertEquals(NesByte(0xFF), cpu.registers.a)
        assertFalse(cpu.registers.ps.zeroFlag)
        assertTrue(cpu.registers.ps.negativeFlag)

        // TODO test AbsoluteX, AbsoluteY and IndirectIndexed crossing pages and spending 1 cycle more
    }

    @Test
    fun PHATest() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.PHA, AddressingMode.Implied)

        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        ram.write(ram.STACK_ADDRESS + cpu.registers.sp, NesByte(0xFF))
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.a = NesByte(0)
        cpu.clock()

        assertEquals(cpu.registers.a, ram.read(ram.STACK_ADDRESS + cpu.registers.sp + 1 ))
    }

    @Test
    fun PHPTest() {
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.PHP, AddressingMode.Implied)

        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.ps.negativeFlag = true
        cpu.registers.ps.carryBit = true
        cpu.registers.ps.zeroFlag = true
        cpu.registers.ps.overflowFlag = true
        cpu.registers.ps.interruptDisabled = true
        cpu.registers.ps.decimalMode = true
        cpu.registers.ps.breakCommand = true
        cpu.registers.ps.unusedFlag = true
        cpu.clock()

        assertEquals(cpu.registers.ps.dump(), ram.read(ram.STACK_ADDRESS + cpu.registers.sp + 1))
    }

    @Test
    fun PLATest() {
        // Pull accumulator
        // Scenario 1
        cpu.reset()
        var instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.PHA, AddressingMode.Implied)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.a = NesByte(0x02)
        cpu.clock()

        instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.PLA, AddressingMode.Implied)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.a = NesByte(0)
        cpu.clock()

        assertEquals(NesByte(0x02), cpu.registers.a)
        assertFalse(cpu.registers.ps.negativeFlag)
        assertFalse(cpu.registers.ps.zeroFlag)

        // Scenario 2: negative value is pulled from stack
        cpu.reset()
        instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.PHA, AddressingMode.Implied)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.a = NesByte(0x80)
        cpu.clock()

        instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.PLA, AddressingMode.Implied)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.a = NesByte(0)
        cpu.clock()

        assertEquals(NesByte(0x80), cpu.registers.a)
        assertTrue(cpu.registers.ps.negativeFlag)
        assertFalse(cpu.registers.ps.zeroFlag)

        // Scenario 3: zero value is pulled from stack
        cpu.reset()
        instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.PHA, AddressingMode.Implied)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.a = NesByte(0x00)
        cpu.clock()

        instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.PLA, AddressingMode.Implied)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.a = NesByte(0xFF)
        cpu.clock()

        assertEquals(NesByte(0x00), cpu.registers.a)
        assertFalse(cpu.registers.ps.negativeFlag)
        assertTrue(cpu.registers.ps.zeroFlag)
    }

    @Test
    fun PLPTest() {
        // Pull processor status
        // Scenario 1
        cpu.reset()
        var instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.PHP, AddressingMode.Implied)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.ps.negativeFlag = true
        cpu.registers.ps.carryBit = true
        cpu.registers.ps.zeroFlag = true
        cpu.registers.ps.overflowFlag = true
        cpu.registers.ps.interruptDisabled = true
        cpu.registers.ps.decimalMode = true
        cpu.registers.ps.breakCommand = true
        cpu.registers.ps.unusedFlag = true
        cpu.clock()

        instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.PLP, AddressingMode.Implied)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(NesByte(0xFF), cpu.registers.ps.dump())
    }

    @Test
    fun ROLTest() {
        // Move each of the bits in either A or M one place to the left.
        // Bit 0 is filled with the current value of the carry flag whilst
        // the old bit 7 becomes the new carry flag value.
        // Scenario 1: default
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.ROL, AddressingMode.Accumulator)
        cpu.registers.a = NesByte(0b01010101)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(NesByte(0b10101010), cpu.registers.a)
        assertFalse(cpu.registers.ps.carryBit)
        assertFalse(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        cpu.reset()

        // Scenario 2: result is 0
        cpu.registers.a = NesByte(0b10000000)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.clock()

        assertEquals(NesByte(0b00000000), cpu.registers.a)
        assertTrue(cpu.registers.ps.carryBit)
        assertTrue(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        cpu.reset()
    }

    @Test
    fun RORTest() {
        // Move each of the bits in either A or M one place to the right.
        // Bit 7 is filled with the current value of the carry flag whilst
        // the old bit 0 becomes the new carry flag value.

        // Scenario 1: Carry is 0
        val instruction = InstructionDescription.fromOPCodeAddressingMode(OPCode.ROR, AddressingMode.Accumulator)
        cpu.registers.a = NesByte(0b01010101)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.ps.carryBit = false
        cpu.clock()

        assertEquals(NesByte(0b00101010), cpu.registers.a)
        assertTrue(cpu.registers.ps.carryBit)
        assertFalse(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        cpu.reset()

        // Scenario 1: Carry is 0
        cpu.registers.a = NesByte(0b01010100)
        ram.write(ram.PROGRAM_ADDRESS, instruction.opcode)
        cpu.registers.pc = ram.PROGRAM_ADDRESS
        cpu.registers.ps.carryBit = true
        cpu.clock()

        assertEquals(NesByte(0b10101010), cpu.registers.a)
        assertFalse(cpu.registers.ps.carryBit)
        assertFalse(cpu.registers.ps.zeroFlag)
        assertFalse(cpu.registers.ps.negativeFlag)
        cpu.reset()
    }
}