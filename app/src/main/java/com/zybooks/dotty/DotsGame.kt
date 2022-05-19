package com.zybooks.dotty

import java.util.*

const val NUM_COLORS = 5
const val GRID_SIZE = 6
const val INIT_MOVES = 10

enum class DotStatus {
   Added, Rejected, Removed
}

class DotsGame private constructor() {

   var movesLeft = 0
   var score = 0

   private val dotGrid = MutableList(GRID_SIZE) { MutableList(GRID_SIZE) { Dot() } }
   private val selectedDotList = mutableListOf<Dot>()

   val isGameOver: Boolean
      get() = movesLeft == 0

   val selectedDots: List<Dot>
      get() = Collections.unmodifiableList(selectedDotList)

   val lastSelectedDot: Dot?
      get() {
         return if (selectedDotList.isEmpty()) {
            null
         }
         else {
            selectedDotList[selectedDotList.size - 1]
         }
      }

   val lowestSelectedDots: List<Dot>
      get() {
         // Return the lowest selected dot in each column
         val dotList = mutableListOf<Dot>()
         for (col in 0 until GRID_SIZE) {
            for (row in GRID_SIZE - 1 downTo 0) {
               if (dotGrid[row][col].isSelected) {
                  dotList.add(dotGrid[row][col])
                  break
               }
            }
         }
         return dotList
      }

   companion object {
      private var instance: DotsGame? = null

      fun getInstance(): DotsGame {
         if (instance == null) {
            instance = DotsGame()
         }
         return instance!!
      }
   }

   init {
      for (row in 0 until GRID_SIZE) {
         for (col in 0 until GRID_SIZE) {
            dotGrid[row][col].row = row
            dotGrid[row][col].col = col
         }
      }
   }

   fun getDot(row: Int, col: Int): Dot? {
      return if (row >= GRID_SIZE || row < 0 || col >= GRID_SIZE || col < 0) {
         null
      } else {
         dotGrid[row][col]
      }
   }

   // Start a new game
   fun newGame() {
      score = 0
      movesLeft = INIT_MOVES
      for (row in 0 until GRID_SIZE) {
         for (col in 0 until GRID_SIZE) {
            dotGrid[row][col].setRandomColor()
         }
      }
   }

   // Attempt to add or remove the dot to/from selected dots list
   fun processDot(dot: Dot): DotStatus {
      var status = DotStatus.Rejected

      // Check if first dot selected
      if (selectedDotList.isEmpty()) {
         selectedDotList.add(dot)
         dot.isSelected = true
         status = DotStatus.Added
      } else if (!dot.isSelected) {
         // Make sure new dot is same color and adjacent to last selected dot
         val lastDot: Dot? = this.lastSelectedDot
         if (lastDot?.color == dot.color && lastDot.isAdjacent(dot)) {
            selectedDotList.add(dot)
            dot.isSelected = true
            status = DotStatus.Added
         }
      } else if (selectedDotList.size > 1) {
         // Dot is already selected, so remove last dot if backtracking
         val secondLast = selectedDotList[selectedDotList.size - 2]
         if (secondLast == dot) {
            val removedDot = selectedDotList.removeAt(selectedDotList.size - 1)
            removedDot.isSelected = false
            status = DotStatus.Removed
         }
      }
      return status
   }

   // Clear the list of selected dots
   fun clearSelectedDots() {

      // Reset board so none selected
      for (dot in selectedDotList) {
         dot.isSelected = false
      }

      selectedDotList.clear()
   }

   // Call after completing a dot path to relocate the dots and update the score and moves
   fun finishMove() {
      if (selectedDotList.isNotEmpty()) {
         // Sort by row so dots are processed top-down
         selectedDotList.sortedWith(compareBy { it.row })

         // Move all dots above each selected dot down by changing color
         for (dot in selectedDotList) {
            for (row in dot.row downTo 1) {
               val dotCurrent = dotGrid[row][dot.col]
               val dotAbove = dotGrid[row - 1][dot.col]
               dotCurrent.color = dotAbove.color
            }

            // Add new dot at top
            val topDot = dotGrid[0][dot.col]
            topDot.setRandomColor()
         }
         score += selectedDotList.size
         movesLeft--
         clearSelectedDots()
      }
   }
}