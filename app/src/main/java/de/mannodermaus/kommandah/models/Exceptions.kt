package de.mannodermaus.kommandah.models

class SegmentationFault(val line: Int) : RuntimeException("Segmentation Fault at line $line")
class AlreadyExecuted : RuntimeException("Program can't be executed more than once; use #copy() beforehand")
