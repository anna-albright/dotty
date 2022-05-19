package com.zybooks.dotty

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.zybooks.dotty.DotsView.DotsGridListener
import java.util.*
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.widget.Button

class MainActivity : AppCompatActivity() {

   private val dotsGame = DotsGame.getInstance()
   private lateinit var dotsView: DotsView
   private lateinit var movesRemainingTextView: TextView
   private lateinit var scoreTextView: TextView
   private lateinit var soundEffects: SoundEffects

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_main)

      movesRemainingTextView = findViewById(R.id.moves_remaining_text_view)
      scoreTextView = findViewById(R.id.score_text_view)
      dotsView = findViewById(R.id.dots_view)
      dotsView.setGridListener(gridListener)

      findViewById<Button>(R.id.new_game_button).setOnClickListener { newGameClick() }

      soundEffects = SoundEffects.getInstance(applicationContext)

      startNewGame()
   }

   override fun onDestroy() {
      super.onDestroy()
      soundEffects.release()
   }

   private val gridListener = object : DotsGridListener {
      override fun onDotSelected(dot: Dot, status: DotSelectionStatus) {
         // Ignore selections when game is over
         if (dotsGame.isGameOver) return

         // Play first tone when first dot is selected
         if (status == DotSelectionStatus.First) {
            soundEffects.resetTones()
         }

         // Select the dot and play the right tone
         val addStatus = dotsGame.processDot(dot)
         if (addStatus == DotStatus.Added) {
            soundEffects.playTone(true)
         } else if (addStatus == DotStatus.Removed) {
            soundEffects.playTone(false)
         }

         // If done selecting dots then replace selected dots
         if (status === DotSelectionStatus.Last) {
            if (dotsGame.selectedDots.size > 1) {
               dotsView.animateDots()

               // These methods must be called AFTER the animation completes
               //dotsGame.finishMove()
               //updateMovesAndScore()
            } else {
               dotsGame.clearSelectedDots()
            }
         }

         // Display changes to the game
         dotsView.invalidate()
      }

      override fun onAnimationFinished() {
         dotsGame.finishMove()
         dotsView.invalidate()
         updateMovesAndScore()

         if (dotsGame.isGameOver) {
            soundEffects.playGameOver()
         }
      }
   }

   private fun newGameClick() {

      // Animate down off screen
      val screenHeight = this.window.decorView.height.toFloat()
      val moveBoardOff = ObjectAnimator.ofFloat(
         dotsView, "translationY", screenHeight)
      moveBoardOff.duration = 700
      moveBoardOff.start()

      moveBoardOff.addListener(object : AnimatorListenerAdapter() {
         override fun onAnimationEnd(animation: Animator) {
            startNewGame()

            // Animate from above the screen down to default location
            val moveBoardOn = ObjectAnimator.ofFloat(
               dotsView, "translationY", -screenHeight, 0f)
            moveBoardOn.duration = 700
            moveBoardOn.start()
         }
      })
   }

   private fun startNewGame() {
      dotsGame.newGame()
      dotsView.invalidate()
      updateMovesAndScore()
   }

   private fun updateMovesAndScore() {
      movesRemainingTextView.text = String.format(Locale.getDefault(), "%d", dotsGame.movesLeft)
      scoreTextView.text = String.format(Locale.getDefault(), "%d", dotsGame.score)
   }
}