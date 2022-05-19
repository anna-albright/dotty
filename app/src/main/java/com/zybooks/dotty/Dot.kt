package com.zybooks.dotty

import kotlin.math.abs
import kotlin.random.Random

class Dot(var row: Int = 0, var col: Int = 0) {
   var color = Random.nextInt(NUM_COLORS)
   var centerX = 0f
   var centerY = 0f
   var radius = 1f
   var isSelected = false

   fun setRandomColor() {
      color = Random.nextInt(NUM_COLORS)
   }

   fun isAdjacent(dot: Dot): Boolean {
      val colDiff = abs(col - dot.col)
      val rowDiff = abs(row - dot.row)
      return colDiff + rowDiff == 1
   }
}