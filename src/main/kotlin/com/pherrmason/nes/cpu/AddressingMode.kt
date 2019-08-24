package com.pherrmason.nes.cpu

/**
 * The 6502 processor provides several ways in which memory
 * locations can be addressed. Some instructions support several different modes while others may only support one.
 * In addition the two index registers can not always be used interchangeably.
 * This lack of orthogonality in the instruction set is one of the features
 * that makes the 6502 trickier to program well.
 */

enum class AddressingMode {
    // Indexed addressing: use the X or Y register to help determine the address.

    // The address to be accessed by an instruction using indexed zero page addressing is calculated by taking the 8
    // bit zero page address from the instruction and adding the current value of the X/Y register to it.
    // The address calculation wraps around if the sum of the base address and the register exceed $FF
    ZeroPageXIndexed,
    ZeroPageYIndexed,

    // The address to be accessed by an instruction using X register indexed absolute addressing is computed by
    // taking the 16 bit address from the instruction and added the contents of the X register.
    // For example if X contains $92 then an STA $2000,X instruction will store the accumulator at $2092 (e.g. $2000 + $92).
    AbsoluteXIndexed,

    // The Y register indexed absolute addressing mode is the same as the previous mode only with the contents
    // of the Y register added to the 16 bit address from the instruction.
    AbsoluteYIndexed,

    // The instruction contains a 16 bit address which identifies the location of the least significant byte of
    // another 16 bit memory address which is the real target of the instruction.
    Indirect,

    // Used only with the X register.
    // Indexed indirect addressing is normally used in conjunction with a table of address held on zero page.
    // The address of the table is taken from the instruction and the X register added to it (with zero page wrap around)
    // to give the location of the least significant byte of the target address.
    IndexedIndirect,

    // Used only with the Y register
    // Indirect indirect addressing is the most common indirection mode used on the 6502. In instruction contains
    // the zero page location of the least significant byte of 16 bit address. The Y register is dynamically added to
    // this value to generated the actual target address for operation.
    IndirectIndexed,

    // Non-Indexed, Non Memory
    // -------------------------------------

    // Some instructions have an option to operate directly upon the accumulator. The programmer specifies this by
    // using a special operand value, 'A'.
    Accumulator,

    // Immediate addressing allows the programmer to directly specify an 8 bit constant within the instruction.
    // It is indicated by a '#' symbol followed by an numeric expression.
    Immediate,

    // For many 6502 instructions the source and destination of the information to be manipulated is implied directly
    // by the function of the instruction itself and no further operand needs to be specified..
    Implied,

    // Non-Indexed memory ops
    // -------------------------------------
    // Used for branch operations, the byte after the opcode is the branch offset, which contain a signed 8 bit
    // relative offset (e.g. -128 to +127) which is added to program counter if the condition is true.
    // As the program counter itself is incremented during instruction execution by two the effective address range
    // for the target instruction must be with -126 to +129 bytes of the branch.
    Relative,

    // Instructions using absolute addressing contain a full 16 bit address to identify the target location.
    Absolute,

    // Capable of addressing the first 256 bytes of the CPU's memory map.
    ZeroPage,
}