package com.pherrmason.nes.cpu

import com.pherrmason.nes.cpu.InstructionCode.*
import kotlin.experimental.or

/**
 * The NES CPU is based on the 6502 processor and runs at approximately 1.79 MHz (1.66 MHz in a PAL NES)
 *
 */
class Cpu {
    private val registers = CpuRegisters();

    // Memory
    // $0000-$07FF 	$0800
    private val ram = ByteArray(64000);


    fun run() {
        //val data = fetchMemory(this.pc)
        val instruction = Instruction.fromMemory(this.registers.pc)

        exe(instruction);
    }

    fun decodeOperationAddress(programCounter: Int, instruction: Instruction): Byte {
        var value: Byte = 0;

        when (instruction.mode) {
            AddressingMode.Immediate -> {
                // 2 bytes
                value = this.ram[programCounter + 1];
            }

            AddressingMode.ZeroPage -> {
                // 2 bytes
                val address = this.ram[programCounter + 1];
                value = this.ram[address.toInt()]
            }

            AddressingMode.ZeroPageXIndexed -> {
                // 2 bytes
                val address = this.ram[programCounter + 1].plus(this.registers.x).and(0xFF);
                value = this.ram[address]
            }

            AddressingMode.ZeroPageYIndexed -> {
                val address = this.ram[programCounter + 1].plus(this.registers.y).and(0xFF);
                value = this.ram[address]
            }

            AddressingMode.Absolute -> {
                // 3 bytes
                val address = this.ram[programCounter + 1]
                    .plus(this.ram[programCounter + 2])

                value = this.ram[address]
            }

            AddressingMode.AbsoluteXIndexed -> {
                // 3 bytes
                val address = this.ram[programCounter + 1]
                    .plus(this.ram[programCounter + 2])
                    .plus(this.registers.x)

                value = this.ram[address]
            }

            AddressingMode.AbsoluteYIndexed -> {
                // 3 bytes
                val address = this.ram[programCounter + 1]
                    .plus(this.ram[programCounter + 2])
                    .plus(this.registers.y)

                value = this.ram[address]
            }

            AddressingMode.Indirect -> {
                val indirectAddress = this.ram[programCounter + 1] + this.ram[programCounter + 2];
                val address = this.ram[indirectAddress] + this.ram[indirectAddress + 1];
                value = this.ram[address]
            }

            AddressingMode.IndirectIndexed -> {
                // 2 bytes
                val indirectAddress = this.ram[programCounter + 1].toInt()
                val address = (this.ram[indirectAddress] + this.registers.x ).and(0xFF)
                value = this.ram[address]
            }

            AddressingMode.IndexedIndirect -> {
                // 2 bytes
                val indirectAddress = this.ram[programCounter + 1].toInt()
                val address = (this.ram[indirectAddress] + this.registers.y ).and(0xFF)
                value = this.ram[address]
            }
            AddressingMode.Accumulator -> TODO()
            AddressingMode.Implied -> TODO()
            AddressingMode.Relative -> TODO()
        }

        return value
    }

    fun exe(instruction: Instruction) {
        when (instruction.instruction) {
            ADC -> {

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

    private fun toImplement(instruction: Instruction) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
