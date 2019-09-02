package com.pherrymason.nes.cpu

import com.pherrymason.nes.Address
import com.pherrymason.nes.cpu.InstructionCode.*
import com.pherrymason.nes.Byte
import com.pherrymason.nes.ProgramCounter
import com.pherrymason.nes.RAM

/**
 * The NES CPU is based on the 6502 processor and runs at approximately 1.79 MHz
 * (1.66 MHz in a PAL NES)
 */
@ExperimentalUnsignedTypes
class CPU6502 (private var ram: RAM){
    public val registers = CpuRegisters();

    // Memory
    // $0000-$07FF 	$0800

    fun reset() {
        this.registers.reset()

        for (address in 1..0xFFFF) {
            ram.write(Address(address), Byte(0x00))
        }
    }

    fun clock() {
        //val data = fetchMemory(this.pc)
        val instruction = Instruction.fromMemory(this.registers.pc)
        val address = this.decodeOperationAddress(this.registers.pc, instruction)
        exe(instruction);
    }

    fun exe(instruction: Instruction) {
        when (instruction.instruction) {
            ADC -> {
                var value = decodeOperationAddress(this.registers.pc, instruction)
            }

            AND -> toImplement(instruction);
            ASL -> toImplement(instruction);
            BCC -> toImplement(instruction);
            BCS -> toImplement(instruction);
            BEQ -> toImplement(instruction);
            BIT -> toImplement(instruction);
            BMI -> toImplement(instruction);
            BNE -> toImplement(instruction);
            BPL -> toImplement(instruction);
            BRK -> toImplement(instruction);
            BVC -> toImplement(instruction);
            BVS -> toImplement(instruction);
            CLC -> toImplement(instruction);
            CLD -> toImplement(instruction);
            CLI -> toImplement(instruction);
            CLV -> toImplement(instruction);
            CMP -> toImplement(instruction);
            CPX -> toImplement(instruction);
            CPY -> toImplement(instruction);
            DEC -> toImplement(instruction);
            DEX -> toImplement(instruction);
            DEY -> toImplement(instruction);
            EOR -> toImplement(instruction);
            INC -> toImplement(instruction);
            INX -> toImplement(instruction);
            INY -> toImplement(instruction);
            JMP -> toImplement(instruction);
            JSR -> toImplement(instruction);
            LDA -> toImplement(instruction);
            LDX -> toImplement(instruction);
            LDY -> toImplement(instruction);
            LSR -> toImplement(instruction);
            NOP -> toImplement(instruction);
            ORA -> toImplement(instruction);
            PHA -> toImplement(instruction);
            PHP -> toImplement(instruction);
            PLA -> toImplement(instruction);
            PLP -> toImplement(instruction);
            ROL -> toImplement(instruction);
            ROR -> toImplement(instruction);
            RTI -> toImplement(instruction);
            RTS -> toImplement(instruction);
            SBC -> toImplement(instruction);
            SEC -> toImplement(instruction);
            SED -> toImplement(instruction);
            SEI -> toImplement(instruction);
            STA -> toImplement(instruction);
            STX -> toImplement(instruction);
            STY -> toImplement(instruction);
            TAX -> toImplement(instruction);
            TAY -> toImplement(instruction);
            TSX -> toImplement(instruction);
            TXA -> toImplement(instruction);
            TXS -> toImplement(instruction);
            TYA -> toImplement(instruction);
        }
    }

    fun decodeOperationAddress(programCounter: ProgramCounter, instruction: Instruction): Address {
        val valor: Byte;
        when (instruction.mode) {
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
                return Address(lo, Byte(0x00))
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
                return evalPointer(programCounter + 1)
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

                if (ptr_lo == Byte(0xFF)) {
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

                val lo = read(Address(pageZeroAddressLO, Byte(0x00)))
                val hi = read(Address(pageZeroAddressLO + 1, Byte(0x00)))

                return Address(lo, hi)
            }

            AddressingMode.PostIndexedIndirect -> {
                // 5+ cycles
                val arg = read(programCounter + 1)

                val lo = read(Address(arg, Byte(0))) + registers.y
                val hi = read(Address(arg + 1, Byte(0))) + registers.y

                return Address(lo, hi)
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

    fun evalPointer(address: Address): Address {
        // return this.ram[address] + this.ram[address + 1].toInt().shl(8)
        val lo = this.ram.read(address)
        val hi = this.ram.read(address + 1)

        return Address(lo, hi)
    }

    fun read(address: Address): Byte {
        return this.ram.read(address)
    }

    private fun toImplement(instruction: Instruction) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
