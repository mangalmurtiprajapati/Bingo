package com.example.model

enum class GameMode(val title: String, val subtitle: String) {
    VS_COMPUTER("Play with Computer", "Challenge smart AI bot"),
    PLAY_WITH_FRIEND("Play with Friends", "Pass & Play on same device"),
    PLAY_ONLINE("Play Online", "Room codes & Quick match")
}

enum class MatchTurn {
    PLAYER_1_TURN,
    PLAYER_2_OR_COMPUTER_TURN
}

enum class MatchOutcome {
    IN_PROGRESS,
    PLAYER_WON,
    COMPUTER_OR_PLAYER_2_WON,
    DRAW
}

data class LineStrike(
    val id: String,
    val name: String,
    val cellIndices: List<Pair<Int, Int>> // (row, col)
)

data class CompletedLineInfo(
    val letter: Char,
    val lineStrike: LineStrike,
    val numbers: List<Int>
)

data class ClassicBingoBoard(
    val cells: List<List<Int?>> = List(5) { List(5) { null } },
    val crossedNumbers: Set<Int> = emptySet()
) {
    companion object {
        val ALL_LINES: List<LineStrike> = buildList {
            // 5 Horizontal rows
            for (r in 0 until 5) {
                add(LineStrike("row_$r", "Row ${r + 1}", (0 until 5).map { c -> Pair(r, c) }))
            }
            // 5 Vertical columns
            for (c in 0 until 5) {
                add(LineStrike("col_$c", "Col ${c + 1}", (0 until 5).map { r -> Pair(r, c) }))
            }
            // 2 Diagonals
            add(LineStrike("diag_main", "Main Diagonal", (0 until 5).map { i -> Pair(i, i) }))
            add(LineStrike("diag_anti", "Anti Diagonal", (0 until 5).map { i -> Pair(i, 4 - i) }))
        }

        fun generateRandomBoard(): ClassicBingoBoard {
            val shuffled = (1..25).shuffled()
            val grid = List(5) { r ->
                List(5) { c ->
                    shuffled[r * 5 + c]
                }
            }
            return ClassicBingoBoard(cells = grid)
        }
    }

    val isFull: Boolean
        get() = cells.all { row -> row.all { it != null } }

    val numbersPlacedCount: Int
        get() = cells.sumOf { row -> row.count { it != null } }

    val nextNumberToPlace: Int
        get() = numbersPlacedCount + 1

    fun assignNextNumber(row: Int, col: Int): ClassicBingoBoard {
        if (cells[row][col] != null || nextNumberToPlace > 25) return this
        val newCells = cells.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, value ->
                if (r == row && c == col) nextNumberToPlace else value
            }
        }
        return copy(cells = newCells)
    }

    fun removeCellNumber(row: Int, col: Int): ClassicBingoBoard {
        val current = cells[row][col] ?: return this
        // Re-number remaining or clear
        val newCells = cells.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, value ->
                if (r == row && c == col) null else value
            }
        }
        return copy(cells = newCells)
    }

    fun clear(): ClassicBingoBoard = ClassicBingoBoard()

    fun isNumberCrossed(number: Int): Boolean = crossedNumbers.contains(number)

    fun crossNumber(number: Int): ClassicBingoBoard {
        if (crossedNumbers.contains(number)) return this
        return copy(crossedNumbers = crossedNumbers + number)
    }

    /**
     * Returns the list of completed lines (where all 5 numbers are crossed).
     */
    fun getCompletedLines(): List<LineStrike> {
        return ALL_LINES.filter { line ->
            line.cellIndices.all { (r, c) ->
                val num = cells.getOrNull(r)?.getOrNull(c)
                num != null && crossedNumbers.contains(num)
            }
        }
    }

    fun getCompletedLineInfos(): List<CompletedLineInfo> {
        val bingoLetters = listOf('B', 'I', 'N', 'G', 'O')
        val completed = getCompletedLines()
        return completed.mapIndexed { index, strike ->
            val letter = if (index < 5) bingoLetters[index] else '+'
            val numbersInLine = strike.cellIndices.mapNotNull { (r, c) -> cells.getOrNull(r)?.getOrNull(c) }
            CompletedLineInfo(letter = letter, lineStrike = strike, numbers = numbersInLine)
        }
    }

    val completedLinesCount: Int
        get() = getCompletedLines().size

    val bingoLettersStruck: Int
        get() = minOf(5, completedLinesCount)

    val hasBingo: Boolean
        get() = completedLinesCount >= 5

    /**
     * For highlighting cells that belong to any completed winning line.
     */
    fun isCellInCompletedLine(row: Int, col: Int): Boolean {
        val completed = getCompletedLines()
        return completed.any { line ->
            line.cellIndices.contains(Pair(row, col))
        }
    }

    fun containsNumber(number: Int): Boolean {
        return cells.any { row -> row.contains(number) }
    }
}

data class FriendProfile(
    val name: String,
    val level: Int,
    val statsDesc: String,
    val isOnline: Boolean = true,
    val avatarEmoji: String = "😎"
)

data class ChatReaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderName: String,
    val senderAvatar: String,
    val isUser: Boolean,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

