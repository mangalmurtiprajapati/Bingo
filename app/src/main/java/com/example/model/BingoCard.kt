package com.example.model

import kotlin.random.Random

data class BingoCell(
    val number: Int,
    val columnLetter: Char,
    val isFreeSpace: Boolean = false,
    val isMarked: Boolean = false,
    val isWinningCell: Boolean = false
)

data class BingoCard(
    val id: Int,
    val grid: List<List<BingoCell>>,
    val isBingoDeclared: Boolean = false,
    val completedPatterns: List<String> = emptyList()
) {
    companion object {
        fun generateRandomCard(cardId: Int = 1): BingoCard {
            val bCol = (1..15).shuffled().take(5)
            val iCol = (16..30).shuffled().take(5)
            val nCol = (31..45).shuffled().take(5)
            val gCol = (46..60).shuffled().take(5)
            val oCol = (61..75).shuffled().take(5)

            val grid = MutableList(5) { row ->
                MutableList(5) { col ->
                    val isFree = row == 2 && col == 2
                    val num = when (col) {
                        0 -> bCol[row]
                        1 -> iCol[row]
                        2 -> if (isFree) 0 else nCol[row]
                        3 -> gCol[row]
                        4 -> oCol[row]
                        else -> 0
                    }
                    val letter = when (col) {
                        0 -> 'B'
                        1 -> 'I'
                        2 -> 'N'
                        3 -> 'G'
                        else -> 'O'
                    }
                    BingoCell(
                        number = num,
                        columnLetter = letter,
                        isFreeSpace = isFree,
                        isMarked = isFree // Center FREE space is automatically marked
                    )
                }
            }

            return BingoCard(id = cardId, grid = grid)
        }
    }

    /**
     * Toggles/marks a number on the card if called or clicked.
     */
    fun markNumber(number: Int): BingoCard {
        var changed = false
        val newGrid = grid.map { row ->
            row.map { cell ->
                if (!cell.isMarked && cell.number == number) {
                    changed = true
                    cell.copy(isMarked = true)
                } else {
                    cell
                }
            }
        }
        return if (changed) copy(grid = newGrid) else this
    }

    /**
     * Auto daub numbers that have been called in the game.
     */
    fun autoDaub(calledNumbers: Set<Int>): BingoCard {
        val newGrid = grid.map { row ->
            row.map { cell ->
                if (!cell.isMarked && (cell.isFreeSpace || calledNumbers.contains(cell.number))) {
                    cell.copy(isMarked = true)
                } else cell
            }
        }
        return copy(grid = newGrid)
    }

    /**
     * Checks if the card has any valid winning BINGO patterns.
     * Returns a pair of (hasBingo, list of winning patterns, card with highlighted winning cells)
     */
    fun checkBingoPatterns(): Triple<Boolean, List<String>, BingoCard> {
        val newWinningGrid = grid.map { row -> row.map { cell -> cell.copy(isWinningCell = false) }.toMutableList() }
        val patternsFound = mutableListOf<String>()

        // 1. Check Rows
        for (r in 0 until 5) {
            if (grid[r].all { it.isMarked }) {
                patternsFound.add("Row ${r + 1}")
                for (c in 0 until 5) {
                    newWinningGrid[r][c] = newWinningGrid[r][c].copy(isWinningCell = true)
                }
            }
        }

        // 2. Check Columns
        for (c in 0 until 5) {
            if ((0 until 5).all { r -> grid[r][c].isMarked }) {
                val colLetter = when(c) { 0 -> 'B'; 1 -> 'I'; 2 -> 'N'; 3 -> 'G'; else -> 'O' }
                patternsFound.add("Column $colLetter")
                for (r in 0 until 5) {
                    newWinningGrid[r][c] = newWinningGrid[r][c].copy(isWinningCell = true)
                }
            }
        }

        // 3. Diagonal Top-Left to Bottom-Right
        if ((0 until 5).all { i -> grid[i][i].isMarked }) {
            patternsFound.add("Main Diagonal")
            for (i in 0 until 5) {
                newWinningGrid[i][i] = newWinningGrid[i][i].copy(isWinningCell = true)
            }
        }

        // 4. Diagonal Top-Right to Bottom-Left
        if ((0 until 5).all { i -> grid[i][4 - i].isMarked }) {
            patternsFound.add("Anti Diagonal")
            for (i in 0 until 5) {
                newWinningGrid[i][4 - i] = newWinningGrid[i][4 - i].copy(isWinningCell = true)
            }
        }

        // 5. Four Corners
        if (grid[0][0].isMarked && grid[0][4].isMarked && grid[4][0].isMarked && grid[4][4].isMarked) {
            patternsFound.add("Four Corners")
            newWinningGrid[0][0] = newWinningGrid[0][0].copy(isWinningCell = true)
            newWinningGrid[0][4] = newWinningGrid[0][4].copy(isWinningCell = true)
            newWinningGrid[4][0] = newWinningGrid[4][0].copy(isWinningCell = true)
            newWinningGrid[4][4] = newWinningGrid[4][4].copy(isWinningCell = true)
        }

        // 6. Full House (All 25 marked)
        val isFullHouse = grid.all { row -> row.all { it.isMarked } }
        if (isFullHouse) {
            patternsFound.add("FULL HOUSE!")
        }

        val hasBingo = patternsFound.isNotEmpty()
        val updatedCard = copy(
            grid = newWinningGrid,
            isBingoDeclared = hasBingo,
            completedPatterns = patternsFound
        )
        return Triple(hasBingo, patternsFound, updatedCard)
    }
}
