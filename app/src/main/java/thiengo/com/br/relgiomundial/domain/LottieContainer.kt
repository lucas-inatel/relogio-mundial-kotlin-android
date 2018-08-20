/*
 * Developed by: Inatel Competence Center
 * Copyright 2018, NONUS
 * All rights are reserved. Reproduction in whole or part is
 * prohibited without the written consent of the copyright owner.
 */
package thiengo.com.br.relgiomundial.domain

import android.animation.Animator
import android.content.Context
import com.airbnb.lottie.LottieAnimationView
import thiengo.com.br.relgiomundial.ClockActivity
import java.util.*

/**
 *
 * Creation 20/08/2018
 * @author lucasmarciano
 * @version 0.0.0
 */
class LottieContainer(val context: Context, private val animation: LottieAnimationView) : Animator.AnimatorListener {


    companion object {
        const val FRAME_FIRST = 46
        const val FRAME_LAST = 82
        const val SPEED_SUN_TO_MOON = 1.5F
        const val SPEED_MOON_TO_SUN = -1.5F
    }

    var hour: Int = 0

    init {
        animation.setMinAndMaxFrame(FRAME_FIRST, FRAME_LAST)
        animation.addAnimatorListener(this)
        val calendar: Calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR_OF_DAY)
    }

    fun updateByHour(hour: Int) {
        if (!canAnimate(hour)) {
            return
        }
        var speedValue = SPEED_SUN_TO_MOON

        if (isMorning(hour)) {
            speedValue = SPEED_MOON_TO_SUN /* Speed negativo. */
        }

        animation.speed = speedValue
        animation.playAnimation()
        this.hour = hour
        (context as ClockActivity).setNewColors(isMorning(this.hour))
    }

    /**
     * Não há problemas em manter aqui os valores mágicos 6 e 18,
     * pois essa é a definição de "dia (sol)" para o nosso domínio
     * de problema.
     * */
    private fun isMorning(hour: Int): Boolean = hour in 6..17

    /**
     * Método responsável por verificar se a hora atualmente
     * definida, de acordo com a seleção em Spinner, exige que a
     * animação aconteça, ou seja, se for uma hora que indica "dia"
     * e a animação atualmente esteja em "noite", então a animação
     * de "noite" para "dia" deve ocorrer e vice-versa.
     * */
    private fun canAnimate(hour: Int): Boolean =
            (isMorning(hour) && animation.frame == FRAME_LAST)
                    || (!isMorning(hour) && animation.frame == FRAME_FIRST)

    /**
     *
     * Notifies the repetition of the animation.
     *
     * @param animation The animation which was repeated.
     */
    override fun onAnimationRepeat(animation: Animator?) {}

    /**
     *
     * Notifies the end of the animation. This callback is not invoked
     * for animations with repeat count set to INFINITE.
     *
     * @param ani The animation which reached its end.
     */
    override fun onAnimationEnd(ani: Animator?) {
        animation.frame = if (isMorning(hour)) FRAME_FIRST else FRAME_LAST
    }

    /**
     *
     * Notifies the cancellation of the animation. This callback is not invoked
     * for animations with repeat count set to INFINITE.
     *
     * @param animation The animation which was canceled.
     */
    override fun onAnimationCancel(animation: Animator?) {}

    /**
     *
     * Notifies the start of the animation.
     *
     * @param animation The started animation.
     */
    override fun onAnimationStart(animation: Animator?) {}
}