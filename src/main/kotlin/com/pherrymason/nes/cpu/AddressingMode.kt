package com.pherrymason.nes.cpu

/**
 * The 6502 processor provides several ways in which memory
 * locations can be addressed. Some instructions support several different modes while others may only support one.
 * In addition the two index registers can not always be used interchangeably.
 * This lack of orthogonality in the instruction set is one of the features
 * that makes the 6502 trickier to program well.
 */

enum class AddressingMode {
    // This form of addressing is represented with a one byte instruction, IMPLYING an operation on the accumulator
    Accumulator,

    // The second byte of the instruction contains the operand, with no further memory addressing required.
    // Immediate addressing allows the programmer to directly specify an 8 bit constant within the instruction.
    // It is indicated by a '#' symbol followed by an numeric expression.
    Immediate,

    // Instructions using absolute addressing contain a full 16 bit address to identify the target location.
    // The second byte of the instruction specified the eight lower bits of the effective address
    // while the third byte specifies the eight high order bits.
    Absolute,

    // Capable of addressing the first 256 bytes of the CPU's memory map.
    ZeroPage,

    // ---------------------------
    // Indexed addressing: use the X or Y register to help determine the address.

    // The effective address is calculated by adding the second byte to the contents of the index register.
    // The content of the second byte references a location in page zero. Additionally, no carry is added
    // to the high order eight bits of memory and crossing of page boundaries does not occur.
    ZeroPageX,
    ZeroPageY,

    // The effective address is formed by adding the contents of X or Y  to the address contained in the second and
    // third bytes of the instruction.
    AbsoluteXIndexed,
    AbsoluteYIndexed,

    // The address containing the operand is implicitly stated in the operation code of the instruction
    Implied,

    // Used only with branch instructions and establishes a destination for the conditional branch.
    // The second byte of the instruction is an operand. This operand is an offset which is added to
    // the program counter when the counter is set the next instruction.
    // The range of the offset is 1 byte signed (-128 to +127)
    Relative,

    // The instruction contains a 16 bit address which identifies the location of the least significant byte of
    // another 16 bit memory address which is the real target of the instruction.
    // The second byte of the instruction contains the low order byte of a memory location.
    // The high order eight bits of that memory location are contained in the third byte of the instruction.
    // The contents of the fully specified memory location are the low order byte of the effective address.
    // The next memory location contains the igh order byte of the effective address which is loaded into the
    // sixteen bits of the program counter.
    Indirect,

    // Used only with the X register.
    // -Indexed indirect addressing is normally used in conjunction with a table of address held on zero page.
    // -The address of the table is taken from the instruction and the X register added to it (with zero page wrap
    // around)
    // -to give the location of the least significant byte of the target address.

    // The second byte of the instruction is added to the contents of index register X discarding the carry.
    // The result of this addition points to a memory location on page zero which contains the low order byte of
    // the effective address.
    // The next memory location in page zero contains the high order byte of the effective address.
    // Both memory locations specifying the effective address must be in page zero.
    PreIndexedIndirect,

    // Used only with the Y register
    // Indirect indirect addressing is the most common indirection mode used on the 6502. In instruction contains
    // the zero page location of the least significant byte of 16 bit address. The Y register is dynamically added to
    // this value to generated the actual target address for operation.

    // The second byte of the instruction points to a memory location in page zero.
    // The contents of this memory location are added to the contents of index regsiter Y.
    // The result is the low order byte of the effective address.
    // The carry from this addition is added to the contents of the next page zero memory location to form
    // the high order byte of the effective address.
    PostIndexedIndirect,
}