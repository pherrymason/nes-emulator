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
        val address = this.decodeOperationAddress(this.registers.pc, instruction)
        val operand = read(address)
        exe(instruction, operand);
    }

    fun exe(instructionDescription: InstructionDescription, operand: NesByte) {
        when (instructionDescription.instruction) {
            ADC -> toImplement(instructionDescription)
            AND -> {
                val result = registers.a and operand
                registers.setNegativeFlag(result)
                registers.setZeroFlag(result)
                registers.a = result
            }
            ASL -> toImplement(instructionDescription);
            BCC -> toImplement(instructionDescription);
            BCS -> toImplement(instructionDescription);
            BEQ -> toImplement(instructionDescription);
            BIT -> toImplement(instructionDescription);
            BMI -> toImplement(instructionDescription);
            BNE -> toImplement(instructionDescription);
            BPL -> toImplement(instructionDescription);
            BRK -> {
                // 7 cycles
                // TODO advances pc +2
                registers.pc += 1
                // ignore operand
                registers.pc += 1

                registers.ps.breakCommand = true

                this.ram.write(Address(0x100).plus(registers.sp), registers.pc.lowByte())
                registers.sp -= 1
                this.ram.write(Address(0x100) + registers.sp, registers.pc.highByte())
                registers.sp -= 1

                // Reload the Pc from the vector at 0xFFFE-0xFFFF
            }
            BVC -> toImplement(instructionDescription);
            BVS -> toImplement(instructionDescription);
            CLC -> toImplement(instructionDescription);
            CLD -> toImplement(instructionDescription);
            CLI -> toImplement(instructionDescription);
            CLV -> toImplement(instructionDescription);
            CMP -> toImplement(instructionDescription);
            CPX -> toImplement(instructionDescription);
            CPY -> toImplement(instructionDescription);
            DEC -> toImplement(instructionDescription);
            DEX -> toImplement(instructionDescription);
            DEY -> toImplement(instructionDescription);
            EOR -> toImplement(instructionDescription);
            INC -> toImplement(instructionDescription);
            INX -> toImplement(instructionDescription);
            INY -> toImplement(instructionDescription);
            JMP -> toImplement(instructionDescription);
            JSR -> toImplement(instructionDescription);
            LDA -> toImplement(instructionDescription);
            LDX -> toImplement(instructionDescription);
            LDY -> toImplement(instructionDescription);
            LSR -> toImplement(instructionDescription);
            NOP -> toImplement(instructionDescription);
            ORA -> toImplement(instructionDescription);
            PHA -> toImplement(instructionDescription);
            PHP -> toImplement(instructionDescription);
            PLA -> toImplement(instructionDescription);
            PLP -> toImplement(instructionDescription);
            ROL -> toImplement(instructionDescription);
            ROR -> toImplement(instructionDescription);
            RTI -> toImplement(instructionDescription);
            RTS -> toImplement(instructionDescription);
            SBC -> toImplement(instructionDescription);
            SEC -> toImplement(instructionDescription);
            SED -> toImplement(instructionDescription);
            SEI -> toImplement(instructionDescription);
            STA -> toImplement(instructionDescription);
            STX -> toImplement(instructionDescription);
            STY -> toImplement(instructionDescription);
            TAX -> toImplement(instructionDescription);
            TAY -> toImplement(instructionDescription);
            TSX -> toImplement(instructionDescription);
            TXA -> toImplement(instructionDescription);
            TXS -> toImplement(instructionDescription);
            TYA -> toImplement(instructionDescription);
        }
    }

    fun decodeOperationAddress(
        programCounter: ProgramCounter,
        instructionDescription: InstructionDescription
    ): Address {
        when (instructionDescription.mode) {
            AddressingMode.Immediate -> {
                // 2 bytes
                //value = this.ram[programCounter + 1];
                return programCounter + 1;
            }

            AddressingMode.ZeroPage -> {
                // 2 bytes
                //val address = this.ram[(programCounter + 1u).toInt()];
                //value = this.ram[address.toInt()]
                val lo = this.ram.read(programCounter + 1)
                return Address(lo, NesByte(0x00))
            }

            AddressingMode.ZeroPageX -> {
                // 2 bytes
                //val address = this.ram[programCounter + 1].plus(this.registers.x).and(0xFF);
                //value = this.ram[address]

                val lo = this.ram.read(programCounter + 1).plus(this.registers.x)
                return Address(lo.and(0xFF), 0x00)
            }

            AddressingMode.ZeroPageY -> {
                //val address = this.ram[programCounter + 1].plus(this.registers.y).and(0xFF);
                //value = this.ram[address]
                val lo = this.ram.read(programCounter + 1).plus(this.registers.y)
                return Address(lo.and(0xFF), 0x00)
            }

            AddressingMode.Absolute -> {
                // 3 bytes
                //val address = this.readAddressAt(programCounter + 1)
                //value = this.ram[address]
                val lo = this.ram.read(programCounter + 1)
                val hi = this.ram.read(programCounter + 2)

                return Address(lo, hi)
            }

            AddressingMode.AbsoluteXIndexed -> {
                // 3 bytes
                //val address = this.ram[programCounter + 1]
                //    .plus(this.ram[programCounter + 2])
                //    .plus(this.registers.x)

                //value = this.ram[address]
                val lo = this.ram.read(programCounter + 1)
                val hi = this.ram.read(programCounter + 2)
                val address = Address(lo, hi)

                // Should check here if address changed page: need to indicate
                // operation might take one cpu cycle more.

                return address.plus(this.registers.x)
            }

            AddressingMode.AbsoluteYIndexed -> {
                // 3 bytes
                //val address = this.ram[programCounter + 1]
                //    .plus(this.ram[programCounter + 2])
                //    .plus(this.registers.y)
                //value = this.ram[address]

                val lo = this.ram.read(programCounter + 1)
                val hi = this.ram.read(programCounter + 2)
                val address = Address(lo, hi)

                // Should check here if address changed page: need to indicate
                // operation might take one cpu cycle more.
                return address.plus(this.registers.y)
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
                //val indirectAddress = this.ram[programCounter + 1] + this.ram[programCounter + 2]
                //val address = this.readAddressAt(indirectAddress)
                //value = this.ram[address]
                val ptr_lo = this.ram.read(programCounter + 1)
                val ptr_hi = this.ram.read(programCounter + 2)
                val ptr_address = Address(ptr_lo, ptr_hi)

                if (ptr_lo == NesByte(0xFF)) {
                    // Simulate page boundary hardware bug
                    val fixHi = ptr_address.and(0xFF00).shl(8)
                    val lo = read(fixHi)
                    val hi = read(ptr_address)

                    return Address(lo, hi)
                }

                // Behave normally
                val lo = read(ptr_address)
                val hi = read(ptr_address.plus(1))
                return Address(lo, hi)
            }

            AddressingMode.PreIndexedIndirect -> {
                // 6 cycles
                // 2 bytes
                val arg = read(programCounter + 1)
                val pageZeroAddressLO = (arg + this.registers.x) and 0xFF

                val lo = read(Address(pageZeroAddressLO, NesByte(0x00)))
                val hi = read(Address(pageZeroAddressLO + 1, NesByte(0x00)))

                return Address(lo, hi)
            }

            AddressingMode.PostIndexedIndirect -> {
                // 5+ cycles
                val arg = read(programCounter + 1)
                val lo = read(Address(arg, NesByte(0)))
                val hi = read(Address(arg + 1 and 0xFF, NesByte(0)))

                var address = Address(lo, hi)
                address = address.plus(registers.y)

                return address
            }

            AddressingMode.Relative -> {
                var relative = this.ram.read(programCounter + 1)
                relative = relative.minus(128)

                return programCounter.plus(relative)
            }
            else -> {
                //AddressingMode.Accumulator ->
                //AddressingMode.Implied ->
            }
        }

        return Address(0)
    }

    fun read(address: Address): NesByte {
        return this.ram.read(address)
    }

    private fun toImplement(instructionDescription: InstructionDescription) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
