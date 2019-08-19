package com.pherrmason.nes.cpu

import com.pherrmason.nes.cpu.Instruction.*

/**
 * The NES CPU is based on the 6502 processor and runs at approximately 1.79 MHz (1.66 MHz in a PAL NES)
 *
 */
class Cpu {
    private val registers = CpuRegisters();

    fun start() {
        run(ADC);
    }

    fun run(instruction: Instruction) {
        when (instruction) {
            ADC -> toImplement(instruction);
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
