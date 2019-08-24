package com.pherrmason.nes.cpu

enum class InstructionCode {
    ADC, AND, ASL, BCC, BCS, BEQ, BIT, BMI, BNE,
    BPL, BRK, BVC, BVS, CLC, CLD, CLI, CLV, CMP,
    CPX, CPY, DEC, DEX, DEY, EOR, INC, INX, INY,
    JMP, JSR, LDA, LDX, LDY, LSR, NOP, ORA, PHA,
    PHP, PLA, PLP, ROL, ROR, RTI, RTS, SBC, SEC,
    SED, SEI, STA, STX, STY, TAX, TAY, TSX, TXA,
    TXS, TYA
}

/*class Opcode (Instruction, AddressMode, X) {

}*/


/*val opcodes = mapOf(
    0x29 to Opcode(ADC, Immediate, asdasd),
)*/

enum class Instruction(val opcode: Byte, val instruction: InstructionCode, val mode: AddressingMode) {
    // Add with Carry (ADC)
    AddwithCarry_Immediate(0x69, InstructionCode.ADC, AddressingMode.Immediate),
    AddWithCarry_ZeroPage(0x65, InstructionCode.ADC, AddressingMode.ZeroPage),
    AddWithCarry_ZeroPageIndexed(0x75, InstructionCode.ADC, AddressingMode.ZeroPageXIndexed),
    AddWithCarry_Absolute(0x6D, InstructionCode.ADC, AddressingMode.Absolute),
    AddWithCarry_AbsoluteX(0x7D, InstructionCode.ADC, AddressingMode.AbsoluteXIndexed),
    AddWithCarry_AbsoluteY(0x79, InstructionCode.ADC, AddressingMode.AbsoluteYIndexed),
    AddWithCarry_IndirectX(0x61, InstructionCode.ADC, AddressingMode.IndexedIndirect),
    AddWithCarry_IndirectY(0x71, InstructionCode.ADC, AddressingMode.IndirectIndexed);



    companion object {
        fun fromMemory(pc: Any): Instruction = values().first { it.opcode == pc }
    }
}