package de.mannodermaus.kommandah.views.main

import de.mannodermaus.kommandah.models.Instruction

/**
 * Representation of a single entry in a Program, exposed to the View layer.
 * Note that the included [Instruction] might be absent in cases where
 * there are "empty cells" in the program in-between routines or blocks.
 */
data class InstructionData(val index: Int, val instruction: Instruction?)
