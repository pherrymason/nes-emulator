package com.pherrymason.nes.cpu

import com.pherrymason.nes.*
import com.pherrymason.nes.cpu.OPCode.*

/**
 * The NES CPU is based on the 6502 processor and runs at approximately 1.79 MHz
 * (1.66 MHz in a PAL NES)
 */
@ExperimentalUnsignedTypes
class CPU6502(private var ram: RAM) {
    public val registers = CpuRegisters();

    // Memory
    // $0000-$07FF 	$0800

    fun reset() {
        this.registers.reset()

//        for (address in 1..0xFFFF) {
//            ram.write(Address(address), Byte(0x00))
//        }
    }

    fun clock() {
        val instruction = InstructionDescription.fromMemory(read(this.registers.pc))
        this.registers.pc++

        val decodedAddressResult = this.decodeOperationAddress(this.registers.pc, instruction)
        exe(instruction, decodedAddressResult)
    }

    fun exe(instructionDescription: InstructionDescription, decodedAddress: DecodedAddressMode) {
        when (instructionDescription.instruction) {
            ADC -> toImplement(instructionDescription)
            AND -> opAND(decodedAddress)
            ASL -> toImplement(instructionDescription)
            BCC -> opBCC(decodedAddress)
            BCS -> opBCS(decodedAddress)
            BEQ -> opBEQ(decodedAddress)
            BIT -> toImplement(instructionDescription)
            BMI -> opBMI(decodedAddress)
            BNE -> opBNE(decodedAddress)
            BPL -> opBPL(decodedAddress)
            BRK -> opBRK(decodedAddress)
            BVC -> opBVC(decodedAddress)
            BVS -> opBVS(decodedAddress)
            CLC -> opCLC(decodedAddress)
            CLD -> opCLD(decodedAddress)
            CLI -> opCLI(decodedAddress)
            CLV -> opCLV(decodedAddress)
            CMP -> opCMP(decodedAddress)
            CPX -> opCPX(decodedAddress)
            CPY -> opCPY(decodedAddress)
            DEC -> opDEC(decodedAddress)
            DEX -> opDEX(decodedAddress)
            DEY -> opDEY(decodedAddress)
            EOR -> toImplement(instructionDescription)
            INC -> opINC(decodedAddress)
            INX -> opINX(decodedAddress)
            INY -> opINY(decodedAddress)
            JMP -> opJMP(decodedAddress)
            JSR -> opJSR(decodedAddress)
            LDA -> opLDA(decodedAddress)
            LDX -> opLDX(decodedAddress)
            LDY -> opLDY(decodedAddress)
            LSR -> opLSR(instructionDescription, decodedAddress)
            NOP -> opNOP()
            ORA -> opORA(decodedAddress)
            PHA -> opPHA(decodedAddress)
            PHP -> opPHP(decodedAddress)
            PLA -> opPLA(decodedAddress)
            PLP -> opPLP(decodedAddress)
            ROL -> opROL(instructionDescription, decodedAddress)
            ROR -> opROR(instructionDescription, decodedAddress)
            RTI -> opRTI()
            RTS -> toImplement(instructionDescription)
            SBC -> toImplement(instructionDescription)
            SEC -> toImplement(instructionDescription)
            SED -> toImplement(instructionDescription)
            SEI -> toImplement(instructionDescription)
            STA -> toImplement(instructionDescription)
            STX -> toImplement(instructionDescription)
            STY -> toImplement(instructionDescription)
            TAX -> toImplement(instructionDescription)
            TAY -> toImplement(instructionDescription)
            TSX -> toImplement(instructionDescription)
            TXA -> toImplement(instructionDescription)
            TXS -> toImplement(instructionDescription)
            TYA -> toImplement(instructionDescription)
        }
    }

    fun decodeOperationAddress(
        programCounter: ProgramCounter,
        instructionDescription: InstructionDescription
    ): DecodedAddressMode {
        when (instructionDescription.mode) {
            AddressingMode.Immediate -> {
                // 2 bytes
                return DecodedAddressMode(programCounter)
            }

            AddressingMode.ZeroPage -> {
                // 2 bytes
                val lo = this.ram.read(programCounter)
                return DecodedAddressMode(Address(lo, NesByte(0x00)))
            }

            AddressingMode.ZeroPageX -> {
                // 2 bytes
                val lo = this.ram.read(programCounter ).plus(this.registers.x)
                return DecodedAddressMode(Address(lo.and(0xFF), 0x00))
            }

            AddressingMode.ZeroPageY -> {
                //val address = this.ram[programCounter + 1].plus(this.registers.y).and(0xFF);
                //value = this.ram[address]
                val lo = this.ram.read(programCounter).plus(this.registers.y)
                return DecodedAddressMode(Address(lo.and(0xFF), 0x00))
            }

            AddressingMode.Absolute -> {
                // 3 bytes
                val lo = this.ram.read(programCounter)

                // Bug: Missing incrementing programCounter
                val hi = this.ram.read(programCounter + 1)

                return DecodedAddressMode(Address(lo, hi))
            }

            AddressingMode.AbsoluteXIndexed -> {
                // 3 bytes

                //value = this.ram[address]
                val lo = this.ram.read(programCounter)
                // Bug: Missing incrementing programCounter
                val hi = this.ram.read(programCounter + 1)
                val address = Address(lo, hi)

                // Should check here if address changed page: need to indicate
                // operation might take one cpu cycle more.

                return DecodedAddressMode(address.plus(this.registers.x))
            }

            AddressingMode.AbsoluteYIndexed -> {
                // 3 bytes

                val lo = this.ram.read(programCounter)
                // Bug: Missing incrementing programCounter
                val hi = this.ram.read(programCounter + 1)
                val address = Address(lo, hi)

                // Should check here if address changed page: need to indicate
                // operation might take one cpu cycle more.
                return DecodedAddressMode(address.plus(this.registers.y))
            }

            // Address Mode: Indirect
            // The supplied 16-bit address is read to get the actual 16-bit address. This is
            // instruction is unusual in that it has a bug in the hardware! To emulate its
            // function accurately, we also need to emulate this bug. If the low byte of the
            // supplied address is 0xFF, then to read the high byte of the actual address
            // we need to cross a page boundary. This doesnt actually work on the chip as
            // designed, instead it wraps back around in the same page, yielding an
            // invalid actual address
            AddressingMode.Indirect -> {
                val ptr_lo = this.ram.read(programCounter)
                // Bug: Missing incrementing programCounter
                val ptr_hi = this.ram.read(programCounter + 1)
                val ptr_address = Address(ptr_lo, ptr_hi)

                if (ptr_lo == NesByte(0xFF)) {
                    // Simulate page boundary hardware bug
                    val fixHi = ptr_address.and(0xFF00).shl(8)
                    val lo = read(fixHi)
                    val hi = read(ptr_address)

                    return DecodedAddressMode(Address(lo, hi))
                }

                // Behave normally
                val lo = read(ptr_address)
                val hi = read(ptr_address.plus(1))
                return DecodedAddressMode(Address(lo, hi))
            }

            AddressingMode.PreIndexedIndirect -> {
                // 6 cycles
                // 2 bytes
                val arg = read(programCounter)
                val pageZeroAddressLO = (arg + this.registers.x) and 0xFF

                val lo = read(Address(pageZeroAddressLO, NesByte(0x00)))
                val hi = read(Address(pageZeroAddressLO + 1, NesByte(0x00)))

                return DecodedAddressMode(Address(lo, hi))
            }

            AddressingMode.PostIndexedIndirect -> {
                // 5+ cycles
                val arg = read(programCounter)
                val lo = read(Address(arg, NesByte(0)))
                val hi = read(Address(arg + 1 and 0xFF, NesByte(0)))

                var address = Address(lo, hi)
                address = address.plus(registers.y)

                return DecodedAddressMode(address)
            }

            AddressingMode.Relative -> {
                var valor : Int = this.ram.read(programCounter).byte.toInt()
                valor = valor.minus(128)

                val address = programCounter.plus(valor)

                // bug: absolute address is wrong because we need to take into account
                // the length of the current opcode. PC is still pointing to start of instruction
                return DecodedAddressMode(address, Address(NesByte(valor), 0x00))
            }
            else -> {
                //AddressingMode.Accumulator ->
                //AddressingMode.Implied ->
            }
        }

        return DecodedAddressMode(Address(0))
    }

    fun read(address: Address): NesByte {
        return this.ram.read(address)
    }

    fun write(address: Address, value: NesByte) {
        this.ram.write(address, value)
    }

    fun pullStack(): NesByte {
        registers.sp++
        return read(ram.STACK_ADDRESS + registers.sp)
    }

    fun pushStack(value: NesByte) {
        write(ram.STACK_ADDRESS + registers.sp, value)
        registers.sp--
    }

    private fun toImplement(instructionDescription: InstructionDescription) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun opAND(decodedAddress: DecodedAddressMode) {
        val operand = read(decodedAddress.address)
        val result = registers.a and operand
        registers.ps.updateZeroFlag(result)
        registers.ps.updateNegativeFlag(result)
        registers.a = result
    }

    private fun opBRK(decodedAddress: DecodedAddressMode) {
        // 7 cycles
        // BRK/IRQ/NMI first change the B-flag, then write P to stack, and then set the I-flag,
        // the D-flag is NOT changed and should be cleared by software.

        // ignore operand
        registers.pc += 1

        registers.ps.breakCommand = true

        // Store PC into the stack
        pushStack(registers.pc.lowByte())
        pushStack(registers.pc.highByte())

        // Is it really important to enable this interrupt after storing PC on stack?
        registers.ps.interruptDisabled = true

        // Store PS into the stack
        pushStack(registers.ps.dump())

        registers.ps.breakCommand = false;

        // Reload the Pc from the vector at 0xFFFE-0xFFFF
        val loByte = ram.read(Address(0xFFFE))
        val hiByte = ram.read(Address(0xFFFF))
        registers.pc = Address(loByte, hiByte)
    }

    private fun opBCC(decodedAddress: DecodedAddressMode) {
        registers.pc++

        if (!registers.ps.carryBit) {
            registers.pc+= decodedAddress.relativeAddress ?: throw IllegalAccessError("Missing relative address.")
        }

        // TODO +1 cycle if page changed
    }

    private fun opBCS(decodedAddress: DecodedAddressMode) {
        registers.pc++

        if (registers.ps.carryBit) {
            registers.pc+= decodedAddress.relativeAddress ?: throw IllegalAccessError("Missing relative address.")
        }

        // TODO +1 cycle if page changed
    }

    private fun opBEQ(decodedAddress: DecodedAddressMode) {
        registers.pc++

        if (registers.ps.zeroFlag) {
            registers.pc+= decodedAddress.relativeAddress ?: throw IllegalAccessError("Missing relative address.")
        }
    }

    private fun opBNE(decodedAddress: DecodedAddressMode) {
        registers.pc++

        if (!registers.ps.zeroFlag) {
            registers.pc+= decodedAddress.relativeAddress ?: throw IllegalAccessError("Missing relative address.")
        }
    }

    private fun opBPL(decodedAddress: DecodedAddressMode) {
        registers.pc++

        if (!registers.ps.negativeFlag) {
            registers.pc+= decodedAddress.relativeAddress ?: throw IllegalAccessError("Missing relative address.")
        }
    }

    private fun opBMI(decodedAddress: DecodedAddressMode) {
        registers.pc++

        if (registers.ps.negativeFlag) {
            registers.pc+= decodedAddress.relativeAddress ?: throw IllegalAccessError("Missing relative address.")
        }
    }

    private fun opBVC(decodedAddress: DecodedAddressMode) {
        registers.pc++

        if (!registers.ps.overflowFlag) {
            registers.pc+= decodedAddress.relativeAddress ?: throw IllegalAccessError("Missing relative address.")
        }
    }

    private fun opBVS(decodedAddress: DecodedAddressMode) {
        registers.pc++

        if (registers.ps.overflowFlag) {
            registers.pc+= decodedAddress.relativeAddress ?: throw IllegalAccessError("Missing relative address.")
        }
    }

    private fun opCLC(decodedAddress: DecodedAddressMode) {
        // Two cycles
        registers.ps.carryBit = false
    }

    private fun opCLD(decodedAddress: DecodedAddressMode) {
        // Two cycles
        registers.ps.decimalMode = false
    }

    private fun opCLI(decodedAddress: DecodedAddressMode) {
        // Two cycles
        registers.ps.interruptDisabled = false
    }

    private fun opCLV(decodedAddress: DecodedAddressMode) {
        // Two cycles
        registers.ps.overflowFlag = false
    }

    private fun opCMP(decodedAddress: DecodedAddressMode) {
        registers.pc++
        val operand = read(decodedAddress.address)
        val temp = registers.a.byte.toShort() - operand.byte.toShort()

        registers.ps.carryBit = (operand <= registers.a)
        registers.ps.zeroFlag = (operand == registers.a)
        registers.ps.updateNegativeFlag(NesByte(temp))
    }

    private fun opCPX(decodedAddress: DecodedAddressMode) {
        registers.pc++
        val operand = read(decodedAddress.address)
        val temp = registers.x.byte.toShort() - operand.byte.toShort()

        registers.ps.carryBit = (operand <= registers.x)
        registers.ps.zeroFlag = (operand == registers.x)
        registers.ps.updateNegativeFlag(NesByte(temp))
    }

    private fun opCPY(decodedAddress: DecodedAddressMode) {
        registers.pc++
        val operand = read(decodedAddress.address)
        val temp = registers.y.byte.toShort() - operand.byte.toShort()

        registers.ps.carryBit = (operand <= registers.y)
        registers.ps.zeroFlag = (operand == registers.y)
        registers.ps.updateNegativeFlag(NesByte(temp))
    }

    private fun opDEC(decodedAddress: DecodedAddressMode) {
        registers.pc++
        val operand = read(decodedAddress.address)
        val result = operand.toInt() - 1
        write(decodedAddress.address, NesByte(result and 0xFF))

        registers.ps.updateZeroFlag(NesByte(result))
        registers.ps.updateNegativeFlag(NesByte(result))
    }

    private fun opDEX(decodedAddress: DecodedAddressMode) {
        registers.pc++
        val result = registers.x.toInt() - 1

        registers.x = NesByte(result)
        registers.ps.updateZeroFlag(NesByte(result))
        registers.ps.updateNegativeFlag(NesByte(result))
    }

    private fun opDEY(decodedAddress: DecodedAddressMode) {
        registers.pc++
        val result = registers.y.toInt() - 1

        registers.y = NesByte(result)
        registers.ps.updateZeroFlag(NesByte(result))
        registers.ps.updateNegativeFlag(NesByte(result))
    }

    private fun opINC(decodedAddress: DecodedAddressMode) {
        registers.pc++
        val operand = read(decodedAddress.address)
        val result = operand.toInt() + 1
        write(decodedAddress.address, NesByte(result and 0xFF))

        registers.ps.updateZeroFlag(NesByte(result))
        registers.ps.updateNegativeFlag(NesByte(result))
    }

    private fun opINX(decodedAddress: DecodedAddressMode) {
        registers.pc++
        val operand = registers.x
        val result = operand.toInt() + 1
        registers.x = NesByte(result and 0xFF)

        registers.ps.updateZeroFlag(NesByte(result))
        registers.ps.updateNegativeFlag(NesByte(result))
    }

    private fun opINY(decodedAddress: DecodedAddressMode) {
        registers.pc++
        val operand = registers.y
        val result = operand.toInt() + 1
        registers.y = NesByte(result and 0xFF)

        registers.ps.updateZeroFlag(NesByte(result))
        registers.ps.updateNegativeFlag(NesByte(result))
    }

    private fun opJMP(decodedAddress: DecodedAddressMode) {
        registers.pc++
        registers.pc++

        registers.pc = decodedAddress.address
    }

    private fun opJSR(decodedAddress: DecodedAddressMode) {
        // The JSR instruction pushes the address (minus one)
        // of the return point on to the stack and then sets
        // the program counter to the target memory address.
        registers.pc--
        write(ram.STACK_ADDRESS + registers.sp, registers.pc.highByte())
        registers.sp--

        write(ram.STACK_ADDRESS + registers.sp, registers.pc.lowByte())
        registers.sp--

        registers.pc = decodedAddress.address
    }

    private fun opLDA(decodedAddress: DecodedAddressMode) {
        registers.a = read(decodedAddress.address)
        registers.ps.updateZeroFlag(registers.a)
        registers.ps.updateNegativeFlag(registers.a)
    }

    private fun opLDX(decodedAddress: DecodedAddressMode) {
        registers.x = read(decodedAddress.address)
        registers.ps.updateZeroFlag(registers.x)
        registers.ps.updateNegativeFlag(registers.x)
    }

    private fun opLDY(decodedAddress: DecodedAddressMode) {
        registers.y = read(decodedAddress.address)
        registers.ps.updateZeroFlag(registers.y)
        registers.ps.updateNegativeFlag(registers.y)
    }

    private fun opLSR(instruction: InstructionDescription, decodedAddress: DecodedAddressMode) {
        if (instruction.mode == AddressingMode.Accumulator) {
            registers.ps.carryBit = registers.a and 0x01 == NesByte(1)
            registers.a = registers.a.shr(1)
            registers.ps.updateZeroFlag(registers.a)
        } else {
            val temp = read(decodedAddress.address)
            registers.ps.carryBit = temp and 0x01 == NesByte(1)
            write(decodedAddress.address, temp.shr(1))
            registers.ps.zeroFlag = temp.shr(1) == NesByte(0)
        }
    }

    private fun opNOP() {
        // 1 Cycle
    }

    private fun opORA(decodedAddress: DecodedAddressMode) {
        val temp = read(decodedAddress.address)
        registers.a = registers.a or temp

        registers.ps.updateZeroFlag(registers.a)
        registers.ps.updateNegativeFlag(registers.a)
    }

    private fun opPHA(decodedAddress: DecodedAddressMode) {
        // 3 cycles
        pushStack(registers.a)
    }

    private fun opPHP(decodedAddress: DecodedAddressMode) {
        // 3 Cycles
        pushStack(registers.ps.dump())
    }

    private fun opPLA(decodedAddress: DecodedAddressMode) {
        // 4 Cycles
        registers.a = pullStack()

        registers.ps.updateNegativeFlag(registers.a)
        registers.ps.updateZeroFlag(registers.a)
    }

    private fun opPLP(decodedAddress: DecodedAddressMode) {
        // 4 Cycles
        val temp = pullStack()
        registers.ps.updateFromDump(temp)
    }

    private fun opROL(instruction: InstructionDescription, decodedAddress: DecodedAddressMode) {
        var temp = NesByte(0)
        var bit7 = NesByte(0)
        if (instruction.mode == AddressingMode.Accumulator) {
            temp = registers.a.shl(1)
            bit7 = registers.a.and(0x80).shr(7)
        } else {
            temp = read(decodedAddress.address)
            bit7 = registers.a.and(0x80).shr(7)
            temp = temp.shl(1)
        }

        val bit0 = if (registers.ps.carryBit) 0x01 else 0x00
        temp = temp or NesByte(bit0)
        registers.ps.carryBit = bit7 == NesByte(1)
        registers.ps.updateZeroFlag(temp)

        if (instruction.mode == AddressingMode.Accumulator) {
            registers.a = temp
        } else {
            write(decodedAddress.address, temp)
        }
    }

    private fun opROR(instruction: InstructionDescription, decodedAddress: DecodedAddressMode) {
        var temp = NesByte(0)
        val carryBit = registers.ps.carryBit
        if (instruction.mode == AddressingMode.Accumulator) {
            temp = registers.a
            registers.ps.carryBit = registers.a and 0x01 == NesByte(0x01)
        } else {
            temp = read(decodedAddress.address)
            registers.ps.carryBit = temp and 0x01 == NesByte(0x01)
        }

        val bit7 = if (carryBit) NesByte(0x80) else NesByte(0x00)
        temp = temp.shr(1) or bit7

        if (instruction.mode == AddressingMode.Accumulator) {
            registers.a = temp
        } else {
            write(decodedAddress.address, temp)
        }
    }

    private fun opRTI() {
        val ps = pullStack()
        registers.ps.updateFromDump(ps)

        val lo = pullStack()
        val hi = pullStack()
        registers.pc = Address(lo, hi)
    }
}

@ExperimentalUnsignedTypes
data class DecodedAddressMode(val address: Address, val relativeAddress: Address? = null, val cyclesSpent: Int = 1) {
}
