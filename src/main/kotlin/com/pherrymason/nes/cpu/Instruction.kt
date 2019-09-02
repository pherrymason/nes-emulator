package com.pherrymason.nes.cpu

import com.pherrymason.nes.Byte

enum class InstructionCode {
    ADC, AND, ASL, BCC, BCS, BEQ, BIT, BMI, BNE,
    BPL, BRK, BVC, BVS, CLC, CLD, CLI, CLV, CMP,
    CPX, CPY, DEC, DEX, DEY, EOR, INC, INX, INY,
    JMP, JSR, LDA, LDX, LDY, LSR, NOP, ORA, PHA,
    PHP, PLA, PLP, ROL, ROR, RTI, RTS, SBC, SEC,
    SED, SEI, STA, STX, STY, TAX, TAY, TSX, TXA,
    TXS, TYA
}

@ExperimentalUnsignedTypes
enum class Instruction constructor(val opcode: Byte, val instruction: InstructionCode,
                                   val mode: AddressingMode
) {
    // Add with Carry (ADC)
    AddwithCarry_Immediate(
        Byte(0x69),
        InstructionCode.ADC,
        AddressingMode.Immediate
    ),
    AddWithCarry_ZeroPage(
        Byte(0x65),
        InstructionCode.ADC,
        AddressingMode.ZeroPage
    ),
    AddWithCarry_ZeroPageIndexed(
        Byte(0x75),
        InstructionCode.ADC,
        AddressingMode.ZeroPageX
    ),
    AddWithCarry_Absolute(
        Byte(0x6D),
        InstructionCode.ADC,
        AddressingMode.Absolute
    ),
    AddWithCarry_AbsoluteX(
        Byte(0x7D),
        InstructionCode.ADC,
        AddressingMode.AbsoluteXIndexed
    ),
    AddWithCarry_AbsoluteY(
        Byte(0x79),
        InstructionCode.ADC,
        AddressingMode.AbsoluteYIndexed
    ),
    AddWithCarry_IndirectX(
        Byte(0x61),
        InstructionCode.ADC,
        AddressingMode.PreIndexedIndirect
    ),
    AddWithCarry_IndirectY(
        Byte(0x71),
        InstructionCode.ADC,
        AddressingMode.PostIndexedIndirect
    ),

    LoadXRegister_ZeroPageIndexed(
        Byte(0xB6),
        InstructionCode.LDX,
        AddressingMode.ZeroPageY
    ),

    Branchifcarryflagclear(
        Byte(0x90),
        InstructionCode.BCC,
        AddressingMode.Relative
    );

    companion object {
        fun fromInstructionCode(code: InstructionCode, mode: AddressingMode): Instruction {
            return values().first { it.instruction == code && it.mode == mode }
        }
        fun fromMemory(pc: Any): Instruction = values().first { it.opcode == pc }
    }
}
