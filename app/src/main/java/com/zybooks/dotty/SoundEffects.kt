package com.zybooks.dotty

import android.content.Context
import android.media.SoundPool
import android.media.AudioAttributes

class SoundEffects private constructor(context: Context){

   private var soundPool: SoundPool? = null
   private val selectSoundIds = mutableListOf<Int>()
   private var soundIndex = 0
   private var endGameSoundId = 0

   companion object {
      private var instance: SoundEffects? = null

      fun getInstance(context: Context): SoundEffects {
         if (instance == null) {
            instance = SoundEffects(context)
         }
         return instance!!
      }
   }

   init {
      val attributes = AudioAttributes.Builder()
         .setUsage(AudioAttributes.USAGE_GAME)
         .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
         .build()

      soundPool = SoundPool.Builder()
         .setAudioAttributes(attributes)
         .build()

      soundPool?.let {
         selectSoundIds.add(it.load(context, R.raw.note_e, 1))
         selectSoundIds.add(it.load(context, R.raw.note_f, 1))
         selectSoundIds.add(it.load(context, R.raw.note_f_sharp, 1))
         selectSoundIds.add(it.load(context, R.raw.note_g, 1))

         endGameSoundId = it.load(context, R.raw.game_over, 1)
      }

      resetTones()
   }

   fun resetTones() {
      soundIndex = -1
   }

   fun playTone(advance: Boolean) {
      if (advance) {
         soundIndex++
      } else {
         soundIndex--
      }

      if (soundIndex < 0) {
         soundIndex = 0
      } else if (soundIndex >= selectSoundIds.size) {
         soundIndex = 0
      }

      soundPool?.play(selectSoundIds[soundIndex], 1f, 1f, 1, 0, 1f)
   }

   fun playGameOver() {
      soundPool?.play(endGameSoundId, 0.5f, 0.5f, 1, 0, 1f)
   }

   fun release() {
      soundPool?.release()
      soundPool = null
   }
}