package com.zybooks.dotty

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.MotionEvent
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator

enum class DotSelectionStatus {
   First, Additional, Last
}

const val DOT_RADIUS = 40f

class DotsView(context: Context, attrs: AttributeSet) :
   View(context, attrs) {

   interface DotsGridListener {
      fun onDotSelected(dot: Dot, status: DotSelectionStatus)
      fun onAnimationFinished()
   }

   private val dotsGame = DotsGame.getInstance()
   private val dotPath = Path()
   private var gridListener: DotsGridListener? = null
   private val dotColors = resources.getIntArray(R.array.dotColors)
   private var cellWidth = 0
   private var cellHeight = 0
   private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
   private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG)
   private var animatorSet = AnimatorSet()

   init {
      pathPaint.strokeWidth = 10f
      pathPaint.style = Paint.Style.STROKE
   }

   override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
      val boardWidth = width - paddingLeft - paddingRight
      val boardHeight = height - paddingTop - paddingBottom
      cellWidth = boardWidth / GRID_SIZE
      cellHeight = boardHeight / GRID_SIZE
      resetDots()
   }

   override fun onDraw(canvas: Canvas) {
      super.onDraw(canvas)

      // Draw dots
      for (row in 0 until GRID_SIZE) {
         for (col in 0 until GRID_SIZE) {
            dotsGame.getDot(row, col)?.let {
               dotPaint.color = dotColors[it.color]
               canvas.drawCircle(it.centerX, it.centerY, it.radius, dotPaint)
            }
         }
      }

      if (!animatorSet.isRunning) {

         // Draw connector between selected dots
         val selectedDots = dotsGame.selectedDots
         if (selectedDots.isNotEmpty()) {
            dotPath.reset()
            var dot = selectedDots[0]
            dotPath.moveTo(dot.centerX, dot.centerY)
            for (i in 1 until selectedDots.size) {
               dot = selectedDots[i]
               dotPath.lineTo(dot.centerX, dot.centerY)
            }
            pathPaint.color = dotColors[dot.color]
            canvas.drawPath(dotPath, pathPaint)
         }
      }
   }

   @SuppressLint("ClickableViewAccessibility")
   override fun onTouchEvent(event: MotionEvent): Boolean {

      // Only execute when a listener exists and the animations aren't running
      if (gridListener == null || animatorSet.isRunning) return true

      // Determine which dot is touched
      val col = event.x.toInt() / cellWidth
      val row = event.y.toInt() / cellHeight
      var selectedDot = dotsGame.getDot(row, col)

      // Return previously selected dot if touch moves outside the grid
      if (selectedDot == null) {
         selectedDot = dotsGame.lastSelectedDot
      }

      // Notify activity that a dot is selected
      if (selectedDot != null) {
         when (event.action) {
            MotionEvent.ACTION_DOWN -> {
               gridListener!!.onDotSelected(selectedDot, DotSelectionStatus.First)
            }
            MotionEvent.ACTION_MOVE -> {
               gridListener!!.onDotSelected(selectedDot, DotSelectionStatus.Additional)
            }
            MotionEvent.ACTION_UP -> {
               gridListener!!.onDotSelected(selectedDot, DotSelectionStatus.Last)
            }
         }
      }

      return true
   }

   fun setGridListener(gridListener: DotsGridListener) {
      this.gridListener = gridListener
   }

   fun animateDots() {

      // For storing multiple animations
      val animationList = mutableListOf<Animator>()

      // Get an animation to make selected dots disappear
      animationList.add(getDisappearingAnimator())

      for (dot in dotsGame.lowestSelectedDots) {
         var rowsToMove = 1
         for (row in dot.row - 1 downTo 0) {
            val dotToMove = dotsGame.getDot(row, dot.col)
            dotToMove?.let {
               if (it.isSelected) {
                  rowsToMove++
               } else {
                  val targetY = it.centerY + rowsToMove * cellHeight
                  animationList.add(getFallingAnimator(it, targetY))
               }
            }
         }
      }

      // Play animations (just one right now) together, then reset radius to full size
      animatorSet = AnimatorSet()
      animatorSet.playTogether(animationList)
      animatorSet.addListener(object : AnimatorListenerAdapter() {
         override fun onAnimationEnd(animation: Animator) {
            resetDots()
            gridListener?.onAnimationFinished()
         }
      })
      animatorSet.start()
   }

   private fun getDisappearingAnimator(): ValueAnimator {
      val animator = ValueAnimator.ofFloat(1f, 0f)
      animator.duration = 100
      animator.interpolator = AccelerateInterpolator()
      animator.addUpdateListener { animation: ValueAnimator ->
         for (dot in dotsGame.selectedDots) {
            dot.radius = DOT_RADIUS * animation.animatedValue as Float
         }
         invalidate()
      }
      return animator
   }

   private fun getFallingAnimator(dot: Dot, destinationY: Float): ValueAnimator {
      val animator = ValueAnimator.ofFloat(dot.centerY, destinationY)
      animator.duration = 300
      animator.interpolator = BounceInterpolator()
      animator.addUpdateListener { animation: ValueAnimator ->
         dot.centerY = animation.animatedValue as Float
         invalidate()
      }
      return animator
   }

   private fun resetDots() {
      for (row in 0 until GRID_SIZE) {
         for (col in 0 until GRID_SIZE) {
            dotsGame.getDot(row, col)?.let {
               it.radius = DOT_RADIUS
               it.centerX = col * cellWidth + cellWidth / 2f
               it.centerY = row * cellHeight + cellHeight / 2f
            }
         }
      }
   }
}