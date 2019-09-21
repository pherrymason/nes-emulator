package com.pherrymason.nes.cpu

import com.pherrymason.nes.NesByte

enum class OPCode {
    ADC, AND, ASL, BCC, BCS, BEQ, BIT, BMI, BNE,
    BPL, BRK, BVC, BVS, CLC, CLD, CLI, CLV, CMP,
    CPX, CPY, DEC, DEX, DEY, EOR, INC, INX, INY,
    JMP, JSR, LDA, LDX, LDY, LSR, NOP, ORA, PHA,
    PHP, PLA, PLP, ROL, ROR, RTI, RTS, SBC, SEC,
    SED, SEI, STA, STX, STY, TAX, TAY, TSX, TXA,
    TXS, TYA
}

@ExperimentalUnsignedTypes
enum class InstructionDescription constructor(val opcode: NesByte, val instruction: OPCode,
                                              val mode: AddressingMode
) {
    // AND
    And_Immediate(NesByte(0x29), OPCode.AND, AddressingMode.Immediate),
    And_ZeroPage(NesByte(0x25), OPCode.AND, AddressingMode.ZeroPage),
    And_ZeroPageX(NesByte(0x35), OPCode.AND, AddressingMode.ZeroPageX),
    And_Absolute(NesByte(0x2D), OPCode.AND, AddressingMode.Absolute),
    And_AbsoluteX(NesByte(0x3D), OPCode.AND, AddressingMode.AbsoluteXIndexed),
    And_AbsoluteY(NesByte(0x39), OPCode.AND, AddressingMode.AbsoluteYIndexed),
    And_IndirectX(NesByte(0x21), OPCode.AND, AddressingMode.PostIndexedIndirect),
    And_IndirectY(NesByte(0x31), OPCode.AND, AddressingMode.PreIndexedIndirect),

    // Add with Carry (ADC)
    AddwithCarry_Immediate(NesByte(0x69), OPCode.ADC, AddressingMode.Immediate),
    AddWithCarry_ZeroPage(NesByte(0x65), OPCode.ADC, AddressingMode.ZeroPage),
    AddWithCarry_ZeroPageIndexed(NesByte(0x75), OPCode.ADC, AddressingMode.ZeroPageX),
    AddWithCarry_Absolute(NesByte(0x6D), OPCode.ADC, AddressingMode.Absolute),
    AddWithCarry_AbsoluteX(NesByte(0x7D), OPCode.ADC, AddressingMode.AbsoluteXIndexed),
    AddWithCarry_AbsoluteY(NesByte(0x79), OPCode.ADC, AddressingMode.AbsoluteYIndexed),
    AddWithCarry_IndirectX(NesByte(0x61), OPCode.ADC, AddressingMode.PreIndexedIndirect),
    AddWithCarry_IndirectY(NesByte(0x71), OPCode.ADC, AddressingMode.PostIndexedIndirect),

    ForceBreak(NesByte(0x00), OPCode.BRK, AddressingMode.Implied),

    LoadXRegister_ZeroPageIndexed(NesByte(0xB6), OPCode.LDX, AddressingMode.ZeroPageY),

    BranchIfCarryFlagClear(NesByte(0x90), OPCode.BCC, AddressingMode.Relative),
    BranchIfCarryFlagSet(NesByte(0xB0), OPCode.BCS, AddressingMode.Relative),
    BranchIfEqual(NesByte(0xF0), OPCode.BEQ, AddressingMode.Relative),
    BranchIfNotEqual(NesByte(0xD0), OPCode.BNE, AddressingMode.Relative),
    BranchIfPositive(NesByte(0x10), OPCode.BPL, AddressingMode.Relative),
    BranchIfMinus(NesByte(0x30), OPCode.BMI, AddressingMode.Relative),
    BranchIfOverflowClear(NesByte(0x50), OPCode.BVC, AddressingMode.Relative),
    BranchIfOverflowSet(NesByte(0x70), OPCode.BVS, AddressingMode.Relative),

    ClearCarryFlag(NesByte(0x18), OPCode.CLC, AddressingMode.Implied),
    ClearDecimalMode(NesByte(0xD8), OPCode.CLD, AddressingMode.Implied),
    ClearInterruptFlag(NesByte(0x58), OPCode.CLI, AddressingMode.Implied),
    ClearOverflowFlag(NesByte(0xB8), OPCode.CLV, AddressingMode.Implied),

    Compare_Immediate(NesByte(0xC9), OPCode.CMP, AddressingMode.Immediate),
    Compare_ZeroPage(NesByte(0xC5), OPCode.CMP, AddressingMode.ZeroPage),
    Compare_ZeroPageX(NesByte(0xD5), OPCode.CMP, AddressingMode.ZeroPageX),
    Compare_Absolute(NesByte(0xCD), OPCode.CMP, AddressingMode.Absolute),
    Compare_AbsoluteX(NesByte(0xDD), OPCode.CMP, AddressingMode.AbsoluteXIndexed),
    Compare_AbsoluteY(NesByte(0xD9), OPCode.CMP, AddressingMode.AbsoluteYIndexed),
    Compare_IndirectX(NesByte(0xC1), OPCode.CMP, AddressingMode.PreIndexedIndirect),
    Compare_IndirectY(NesByte(0xD1), OPCode.CMP, AddressingMode.PostIndexedIndirect),

    CompareXRegister_Immediate(NesByte(0xE0), OPCode.CPX, AddressingMode.Immediate),
    CompareXRegister_ZeroPage(NesByte(0xE4), OPCode.CPX, AddressingMode.ZeroPage),
    CompareXRegister_Absolute(NesByte(0xEC), OPCode.CPX, AddressingMode.Absolute),

    CompareYRegister_Immediate(NesByte(0xC0), OPCode.CPY, AddressingMode.Immediate),
    CompareYRegister_ZeroPage(NesByte(0xC4), OPCode.CPY, AddressingMode.ZeroPage),
    CompareYRegister_Absolute(NesByte(0xCC), OPCode.CPY, AddressingMode.Absolute),

    Decrement_ZeroPage(NesByte(0xC6), OPCode.DEC, AddressingMode.ZeroPage),
    Decrement_ZeroPageX(NesByte(0xD6), OPCode.DEC, AddressingMode.ZeroPageX),
    Decrement_Absolute(NesByte(0xCE), OPCode.DEC, AddressingMode.Absolute),
    Decrement_AbsoluteX(NesByte(0xDE), OPCode.DEC, AddressingMode.AbsoluteXIndexed),

    DecrementX(NesByte(0xCA), OPCode.DEX, AddressingMode.Implied),
    DecrementY(NesByte(0x88), OPCode.DEY, AddressingMode.Implied),

    IncrementMemory_ZeroPage(NesByte(0xE6), OPCode.INC, AddressingMode.ZeroPage),
    IncrementMemory_ZeroPageX(NesByte(0xF6), OPCode.INC, AddressingMode.ZeroPageX),
    IncrementMemory_Absolute(NesByte(0xEE), OPCode.INC, AddressingMode.Absolute),
    IncrementMemory_AbsoluteX(NesByte(0xFE), OPCode.INC, AddressingMode.AbsoluteXIndexed),

    IncrementX(NesByte(0xE8), OPCode.INX, AddressingMode.Implied),
    IncrementY(NesByte(0xC8), OPCode.INY, AddressingMode.Implied),

    JumpAbsolute(NesByte(0x4C), OPCode.JMP, AddressingMode.Absolute),
    JumpIndirect(NesByte(0x6C), OPCode.JMP, AddressingMode.Indirect),

    JumptoSubroutine(NesByte(0x20), OPCode.JSR, AddressingMode.Absolute),

    LoadAcumulator_Immediate(NesByte(0xA9), OPCode.LDA, AddressingMode.Immediate),
    LoadAcumulator_ZeroPage(NesByte(0xA5), OPCode.LDA, AddressingMode.ZeroPage),
    LoadAcumulator_ZeroPageX(NesByte(0xB5), OPCode.LDA, AddressingMode.ZeroPageX),
    LoadAcumulator_Absolute(NesByte(0xAD), OPCode.LDA, AddressingMode.Absolute),
    LoadAcumulator_AbsoluteX(NesByte(0xBD), OPCode.LDA, AddressingMode.AbsoluteXIndexed),
    LoadAcumulator_AbsoluteY(NesByte(0xB9), OPCode.LDA, AddressingMode.AbsoluteYIndexed),
    LoadAcumulator_IndirectX(NesByte(0xA1), OPCode.LDA, AddressingMode.PreIndexedIndirect),
    LoadAcumulator_IndirectY(NesByte(0xB1), OPCode.LDA, AddressingMode.PostIndexedIndirect),

    LoadX_Immediate(NesByte(0xA2), OPCode.LDX, AddressingMode.Immediate),
    LoadX_ZeroPage(NesByte(0xA6), OPCode.LDX, AddressingMode.ZeroPage),
    LoadX_ZeroPageY(NesByte(0xB6), OPCode.LDX, AddressingMode.ZeroPageY),
    LoadX_Absolute(NesByte(0xAE), OPCode.LDX, AddressingMode.Absolute),
    LoadX_AbsoluteY(NesByte(0xBE), OPCode.LDX, AddressingMode.AbsoluteYIndexed),

    LoadY_Immediate(NesByte(0xA0), OPCode.LDY, AddressingMode.Immediate),
    LoadY_ZeroPage(NesByte(0xA4), OPCode.LDY, AddressingMode.ZeroPage),
    LoadY_ZeroPageX(NesByte(0xB4), OPCode.LDY, AddressingMode.ZeroPageX),
    LoadY_Absolute(NesByte(0xAC), OPCode.LDY, AddressingMode.Absolute),
    LoadY_AbsoluteX(NesByte(0xBC), OPCode.LDY, AddressingMode.AbsoluteXIndexed),

    LogicalShiftRight_Accumulator(NesByte(0x4A), OPCode.LSR, AddressingMode.Accumulator),
    LogicalShiftRight_ZeroPage(NesByte(0x46), OPCode.LSR, AddressingMode.ZeroPage),
    LogicalShiftRight_ZeroPageX(NesByte(0x56), OPCode.LSR, AddressingMode.ZeroPageX),
    LogicalShiftRight_Absolute(NesByte(0x4E), OPCode.LSR, AddressingMode.Absolute),
    LogicalShiftRight_AbsoluteX(NesByte(0x5E), OPCode.LSR, AddressingMode.AbsoluteXIndexed),

    NOP(NesByte(0xEA), OPCode.NOP, AddressingMode.Immediate),

    LogicalInclusiveOR_Immediate(NesByte(0x09), OPCode.ORA, AddressingMode.Immediate),
    LogicalInclusiveOR_ZeroPage(NesByte(0x05), OPCode.ORA, AddressingMode.ZeroPage),
    LogicalInclusiveOR_ZeroPageX(NesByte(0x15), OPCode.ORA, AddressingMode.ZeroPageX),
    LogicalInclusiveOR_Absolute(NesByte(0x0D), OPCode.ORA, AddressingMode.Absolute),
    LogicalInclusiveOR_AbsoluteX(NesByte(0x1D), OPCode.ORA, AddressingMode.AbsoluteXIndexed),
    LogicalInclusiveOR_AbsoluteY(NesByte(0x19), OPCode.ORA, AddressingMode.AbsoluteYIndexed),
    LogicalInclusiveOR_IndexedIndirect(NesByte(0x01), OPCode.ORA, AddressingMode.PreIndexedIndirect),
    LogicalInclusiveOR_IndirectIndexed(NesByte(0x11), OPCode.ORA, AddressingMode.PostIndexedIndirect),

    PushAccumulator(NesByte(0x48), OPCode.PHA, AddressingMode.Implied),
    PushProcessorStatus(NesByte(0x08), OPCode.PHP, AddressingMode.Implied),
    PullAccumulator(NesByte(0x68), OPCode.PLA, AddressingMode.Implied),
    PullProcessorStatus(NesByte(0x28), OPCode.PLP, AddressingMode.Implied)
    ;

    companion object {
        fun fromOPCodeAddressingMode(code: OPCode, mode: AddressingMode): InstructionDescription {
            return values().first {
                it.instruction == code && it.mode == mode
            }
        }

        fun fromOPCode(code: OPCode): List<InstructionDescription> {
            return values().filter {
                it.instruction == code
            }
        }

        fun fromMemory(value: NesByte): InstructionDescription = values().first {
            it.opcode == value
        }
    }
}
