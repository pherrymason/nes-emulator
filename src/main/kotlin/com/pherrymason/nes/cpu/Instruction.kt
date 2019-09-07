package com.pherrymason.nes.cpu

import com.pherrymason.nes.NesByte

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
enum class Instruction constructor(val opcode: NesByte, val instruction: InstructionCode,
                                   val mode: AddressingMode
) {
    // AND
    And_Immediate(NesByte(0x29), InstructionCode.AND, AddressingMode.Immediate),
    And_ZeroPage(NesByte(0x25), InstructionCode.AND, AddressingMode.ZeroPage),
    And_ZeroPageX(NesByte(0x35), InstructionCode.AND, AddressingMode.ZeroPageX),
    And_Absolute(NesByte(0x2D), InstructionCode.AND, AddressingMode.Absolute),
    And_AbsoluteX(NesByte(0x3D), InstructionCode.AND, AddressingMode.AbsoluteXIndexed),
    And_AbsoluteY(NesByte(0x39), InstructionCode.AND, AddressingMode.AbsoluteYIndexed),
    And_IndirectX(NesByte(0x21), InstructionCode.AND, AddressingMode.PostIndexedIndirect),
    And_IndirectY(NesByte(0x31), InstructionCode.AND, AddressingMode.PreIndexedIndirect),

    // Add with Carry (ADC)
    AddwithCarry_Immediate(
        NesByte(0x69),
        InstructionCode.ADC,
        AddressingMode.Immediate
    ),
    AddWithCarry_ZeroPage(
        NesByte(0x65),
        InstructionCode.ADC,
        AddressingMode.ZeroPage
    ),
    AddWithCarry_ZeroPageIndexed(
        NesByte(0x75),
        InstructionCode.ADC,
        AddressingMode.ZeroPageX
    ),
    AddWithCarry_Absolute(
        NesByte(0x6D),
        InstructionCode.ADC,
        AddressingMode.Absolute
    ),
    AddWithCarry_AbsoluteX(
        NesByte(0x7D),
        InstructionCode.ADC,
        AddressingMode.AbsoluteXIndexed
    ),
    AddWithCarry_AbsoluteY(
        NesByte(0x79),
        InstructionCode.ADC,
        AddressingMode.AbsoluteYIndexed
    ),
    AddWithCarry_IndirectX(
        NesByte(0x61),
        InstructionCode.ADC,
        AddressingMode.PreIndexedIndirect
    ),
    AddWithCarry_IndirectY(
        NesByte(0x71),
        InstructionCode.ADC,
        AddressingMode.PostIndexedIndirect
    ),

    LoadXRegister_ZeroPageIndexed(
        NesByte(0xB6),
        InstructionCode.LDX,
        AddressingMode.ZeroPageY
    ),

    Branchifcarryflagclear(
        NesByte(0x90),
        InstructionCode.BCC,
        AddressingMode.Relative
    ),

    JumpAbsolute(
        NesByte(0x4C),
        InstructionCode.JMP,
        AddressingMode.Absolute
    ),

    JumpIndirect(
        NesByte(0x6C),
        InstructionCode.JMP,
        AddressingMode.Indirect
    );

    companion object {
        fun fromInstructionCode(code: InstructionCode, mode: AddressingMode): Instruction {
            return values().first {
                it.instruction == code && it.mode == mode
            }
        }
        fun fromMemory(value: NesByte): Instruction = values().first {
            it.opcode == value
        }
    }
}
