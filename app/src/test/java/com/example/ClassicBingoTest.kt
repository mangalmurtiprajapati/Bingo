package com.example

import com.example.model.ClassicBingoBoard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassicBingoTest {

    @Test
    fun testBoardAssignmentAndLineCompletion() {
        var board = ClassicBingoBoard()
        assertEquals(0, board.numbersPlacedCount)

        // Assign numbers 1..25 sequentially into the cells
        for (r in 0 until 5) {
            for (c in 0 until 5) {
                board = board.assignNextNumber(r, c)
            }
        }
        assertTrue(board.isFull)
        assertEquals(25, board.numbersPlacedCount)

        // Number at (0,0) should be 1, (0,1) should be 2, etc.
        assertEquals(1, board.cells[0][0])
        assertEquals(5, board.cells[0][4])

        // Cross all numbers in Row 1 (1, 2, 3, 4, 5)
        for (n in 1..5) {
            board = board.crossNumber(n)
        }

        val completedLines = board.getCompletedLineInfos()
        assertEquals(1, completedLines.size)
        assertEquals('B', completedLines[0].letter)
        assertEquals("Row 1", completedLines[0].lineStrike.name)
        assertEquals(listOf(1, 2, 3, 4, 5), completedLines[0].numbers)

        // Cross remaining numbers in Column 1 (6, 11, 16, 21)
        board = board.crossNumber(6)
        board = board.crossNumber(11)
        board = board.crossNumber(16)
        board = board.crossNumber(21)

        val updatedLines = board.getCompletedLineInfos()
        assertEquals(2, updatedLines.size)
        assertEquals('I', updatedLines[1].letter)
        assertEquals("Col 1", updatedLines[1].lineStrike.name)
    }

    @Test
    fun testFiveLinesBingoVictory() {
        var board = ClassicBingoBoard()
        for (r in 0 until 5) {
            for (c in 0 until 5) {
                board = board.assignNextNumber(r, c)
            }
        }
        assertTrue(board.isFull)

        // Cross all 5 rows (1..25)
        for (n in 1..25) {
            board = board.crossNumber(n)
        }

        val completedLines = board.getCompletedLineInfos()
        assertTrue(completedLines.size >= 5)
        assertTrue(board.hasBingo)
        assertEquals(12, completedLines.size) // 5 rows + 5 cols + 2 diags
    }
}
