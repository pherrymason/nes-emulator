package com.pherrymason.nes.cpu

import com.pherrymason.nes.*
import com.pherrymason.nes.cpu.InstructionCode.*

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
            DEC -> toImplement(instructionDescription)
            DEX -> toImplement(instructionDescription)
            DEY -> toImplement(instructionDescription)
            EOR -> toImplement(instructionDescription)
            INC -> toImplement(instructionDescription)
            INX -> toImplement(instructionDescription)
            INY -> toImplement(instructionDescription)
            JMP -> toImplement(instructionDescription)
            JSR -> toImplement(instructionDescription)
            LDA -> toImplement(instructionDescription)
            LDX -> toImplement(instructionDescription)
            LDY -> toImplement(instructionDescription)
            LSR -> toImplement(instructionDescription)
            NOP -> toImplement(instructionDescription)
            ORA -> toImplement(instructionDescription)
            PHA -> toImplement(instructionDescription)
            PHP -> toImplement(instructionDescription)
            PLA -> toImplement(instructionDescription)
            PLP -> toImplement(instructionDescription)
            ROL -> toImplement(instructionDescription)
            ROR -> toImplement(instructionDescription)
            RTI -> toImplement(instructionDescription)
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

    private fun toImplement(instructionDescription: InstructionDescription) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun opAND(decodedAddress: DecodedAddressMode) {
        val operand = read(decodedAddress.address)
        val result = registers.a and operand
        registers.setNegativeFlag(result)
        registers.setZeroFlag(result)
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
        this.ram.write(ram.STACK_ADDRESS + registers.sp, registers.pc.lowByte())
        registers.sp -= 1
        this.ram.write(ram.STACK_ADDRESS + registers.sp, registers.pc.highByte())
        registers.sp -= 1

        // Is it really important to enable this interrupt after storing PC on stack?
        registers.ps.interruptDisabled = true

        // Store PS into the stack
        ram.write(ram.STACK_ADDRESS + registers.sp, registers.ps.dump())

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
        registers.ps.negativeFlag = temp and 0x80 == 0x80
    }

    private fun opCPX(decodedAddress: DecodedAddressMode) {
        registers.pc++
        val operand = read(decodedAddress.address)
        val temp = registers.x.byte.toShort() - operand.byte.toShort()

        registers.ps.carryBit = (operand <= registers.x)
        registers.ps.zeroFlag = (operand == registers.x)
        registers.ps.negativeFlag = temp and 0x80 == 0x80
    }

    private fun opCPY(decodedAddress: DecodedAddressMode) {
        registers.pc++
        val operand = read(decodedAddress.address)
        val temp = registers.y.byte.toShort() - operand.byte.toShort()

        registers.ps.carryBit = (operand <= registers.y)
        registers.ps.zeroFlag = (operand == registers.y)
        registers.ps.negativeFlag = temp and 0x80 == 0x80
    }
}

@ExperimentalUnsignedTypes
data class DecodedAddressMode(val address: Address, val relativeAddress: Address? = null, val cyclesSpent: Int = 1) {
}
