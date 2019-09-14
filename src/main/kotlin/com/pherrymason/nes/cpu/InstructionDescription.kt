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
enum class InstructionDescription constructor(val opcode: NesByte, val instruction: InstructionCode,
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
    AddwithCarry_Immediate(NesByte(0x69), InstructionCode.ADC, AddressingMode.Immediate),
    AddWithCarry_ZeroPage(NesByte(0x65), InstructionCode.ADC, AddressingMode.ZeroPage),
    AddWithCarry_ZeroPageIndexed(NesByte(0x75), InstructionCode.ADC, AddressingMode.ZeroPageX),
    AddWithCarry_Absolute(NesByte(0x6D), InstructionCode.ADC, AddressingMode.Absolute),
    AddWithCarry_AbsoluteX(NesByte(0x7D), InstructionCode.ADC, AddressingMode.AbsoluteXIndexed),
    AddWithCarry_AbsoluteY(NesByte(0x79), InstructionCode.ADC, AddressingMode.AbsoluteYIndexed),
    AddWithCarry_IndirectX(NesByte(0x61), InstructionCode.ADC, AddressingMode.PreIndexedIndirect),
    AddWithCarry_IndirectY(NesByte(0x71), InstructionCode.ADC, AddressingMode.PostIndexedIndirect),

    ForceBreak(NesByte(0x00), InstructionCode.BRK, AddressingMode.Implied),

    LoadXRegister_ZeroPageIndexed(NesByte(0xB6), InstructionCode.LDX, AddressingMode.ZeroPageY),

    BranchIfCarryFlagClear(NesByte(0x90), InstructionCode.BCC, AddressingMode.Relative),
    BranchIfCarryFlagSet(NesByte(0xB0), InstructionCode.BCS, AddressingMode.Relative),
    BranchIfEqual(NesByte(0xF0), InstructionCode.BEQ, AddressingMode.Relative),
    BranchIfNotEqual(NesByte(0xD0), InstructionCode.BNE, AddressingMode.Relative),
    BranchIfPositive(NesByte(0x10), InstructionCode.BPL, AddressingMode.Relative),
    BranchIfMinus(NesByte(0x30), InstructionCode.BMI, AddressingMode.Relative),
    BranchIfOverflowClear(NesByte(0x50), InstructionCode.BVC, AddressingMode.Relative),
    BranchIfOverflowSet(NesByte(0x70), InstructionCode.BVS, AddressingMode.Relative),

    ClearCarryFlag(NesByte(0x18), InstructionCode.CLC, AddressingMode.Implied),
    ClearDecimalMode(NesByte(0xD8), InstructionCode.CLD, AddressingMode.Implied),
    ClearInterruptFlag(NesByte(0x58), InstructionCode.CLI, AddressingMode.Implied),
    ClearOverflowFlag(NesByte(0xB8), InstructionCode.CLV, AddressingMode.Implied),

    Compare_Immediate(NesByte(0xC9), InstructionCode.CMP, AddressingMode.Immediate),
    Compare_ZeroPage(NesByte(0xC5), InstructionCode.CMP, AddressingMode.ZeroPage),
    Compare_ZeroPageX(NesByte(0xD5), InstructionCode.CMP, AddressingMode.ZeroPageX),
    Compare_Absolute(NesByte(0xCD), InstructionCode.CMP, AddressingMode.Absolute),
    Compare_AbsoluteX(NesByte(0xDD), InstructionCode.CMP, AddressingMode.AbsoluteXIndexed),
    Compare_AbsoluteY(NesByte(0xD9), InstructionCode.CMP, AddressingMode.AbsoluteYIndexed),
    Compare_IndirectX(NesByte(0xC1), InstructionCode.CMP, AddressingMode.PreIndexedIndirect),
    Compare_IndirectY(NesByte(0xD1), InstructionCode.CMP, AddressingMode.PostIndexedIndirect),

    CompareXRegister_Immediate(NesByte(0xE0), InstructionCode.CPX, AddressingMode.Immediate),
    CompareXRegister_ZeroPage(NesByte(0xE4), InstructionCode.CPX, AddressingMode.ZeroPage),
    CompareXRegister_Absolute(NesByte(0xEC), InstructionCode.CPX, AddressingMode.Absolute),

    CompareYRegister_Immediate(NesByte(0xC0), InstructionCode.CPY, AddressingMode.Immediate),
    CompareYRegister_ZeroPage(NesByte(0xC4), InstructionCode.CPY, AddressingMode.ZeroPage),
    CompareYRegister_Absolute(NesByte(0xCC), InstructionCode.CPY, AddressingMode.Absolute),

    Decrement_ZeroPage(NesByte(0xC6), InstructionCode.DEC, AddressingMode.ZeroPage),
    Decrement_ZeroPageX(NesByte(0xD6), InstructionCode.DEC, AddressingMode.ZeroPageX),
    Decrement_Absolute(NesByte(0xCE), InstructionCode.DEC, AddressingMode.Absolute),
    Decrement_AbsoluteX(NesByte(0xDE), InstructionCode.DEC, AddressingMode.AbsoluteXIndexed),

    DecrementX(NesByte(0xCA), InstructionCode.DEX, AddressingMode.Implied),
    DecrementY(NesByte(0x88), InstructionCode.DEY, AddressingMode.Implied),

    IncrementMemory_ZeroPage(NesByte(0xE6), InstructionCode.INC, AddressingMode.ZeroPage),
    IncrementMemory_ZeroPageX(NesByte(0xF6), InstructionCode.INC, AddressingMode.ZeroPageX),
    IncrementMemory_Absolute(NesByte(0xEE), InstructionCode.INC, AddressingMode.Absolute),
    IncrementMemory_AbsoluteX(NesByte(0xFE), InstructionCode.INC, AddressingMode.AbsoluteXIndexed),

    IncrementX(NesByte(0xE8), InstructionCode.INX, AddressingMode.Implied),
    IncrementY(NesByte(0xC8), InstructionCode.INY, AddressingMode.Implied),

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
        fun fromInstructionCode(code: InstructionCode, mode: AddressingMode): InstructionDescription {
            return values().first {
                it.instruction == code && it.mode == mode
            }
        }
        fun fromMemory(value: NesByte): InstructionDescription = values().first {
            it.opcode == value
        }
    }
}
