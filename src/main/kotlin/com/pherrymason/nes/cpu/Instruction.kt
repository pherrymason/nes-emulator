package com.pherrymason.nes.cpu

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
enum class Instruction constructor(val opcode: UByte, val instruction: InstructionCode,
                                   val mode: AddressingMode
) {
    // Add with Carry (ADC)
    AddwithCarry_Immediate(0x69u,
        InstructionCode.ADC,
        AddressingMode.Immediate
    ),
    AddWithCarry_ZeroPage(0x65u,
        InstructionCode.ADC,
        AddressingMode.ZeroPage
    ),
    AddWithCarry_ZeroPageIndexed(0x75u,
        InstructionCode.ADC,
        AddressingMode.ZeroPageX
    ),
    AddWithCarry_Absolute(0x6Du,
        InstructionCode.ADC,
        AddressingMode.Absolute
    ),
    AddWithCarry_AbsoluteX(0x7Du,
        InstructionCode.ADC,
        AddressingMode.AbsoluteXIndexed
    ),
    AddWithCarry_AbsoluteY(0x79u,
        InstructionCode.ADC,
        AddressingMode.AbsoluteYIndexed
    ),
    AddWithCarry_IndirectX(0x61u,
        InstructionCode.ADC,
        AddressingMode.PreIndexedIndirect
    ),
    AddWithCarry_IndirectY(0x71u,
        InstructionCode.ADC,
        AddressingMode.PostIndexedIndirect
    ),


    LoadXRegister_ZeroPageIndexed(0xB6u,
        InstructionCode.LDX,
        AddressingMode.ZeroPageY
    );

    companion object {
        fun fromInstructionCode(code: InstructionCode, mode: AddressingMode): Instruction {
            return values().first { it.instruction == code && it.mode == mode }
        }
        fun fromMemory(pc: Any): Instruction = values().first { it.opcode == pc }
    }
}
