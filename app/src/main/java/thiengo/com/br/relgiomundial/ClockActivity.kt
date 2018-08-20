package thiengo.com.br.relgiomundial

import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_clock.*
import thiengo.com.br.relgiomundial.domain.LottieContainer
import java.util.*
import android.view.WindowManager

class ClockActivity :
        AppCompatActivity(),
        AdapterView.OnItemSelectedListener {

    lateinit var countriesGmt: Array<String>
    lateinit var broadcast: BroadcastApplication
    lateinit var lottieContainer: LottieContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)

        initBroadcastReceiver()

        /* Iniciando o array de GMTs. */
        countriesGmt = resources.getStringArray(R.array.countries_gmt)

        /*
         * Vinculando o "listener de item selecionado" ao Spinner
         * de fusos horários.
         * */
        sp_countries.onItemSelectedListener = this
        lottieContainer = LottieContainer(this, lav_sun_moon)
    }

    /**
     * Método responsável por registrar um BroadcastReceiver
     * (BroadcastApplication) para poder receber uma comunicação
     * de TrueTimeApplication, comunicação sobre o retorno de
     * uma data / horário corretos de algum servidor NTP.
     * */
    private fun initBroadcastReceiver() {
        broadcast = BroadcastApplication(this)
        val filter = IntentFilter(BroadcastApplication.FILTER)

        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(broadcast, filter)
    }

    override fun onDestroy() {
        super.onDestroy()

        /* Liberação do BroadcastReceiver. */
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(broadcast)
    }

    /**
     * Listener de novo item selecionado em Spinner. Note que
     * este método é sempre invocado quando a atividade é
     * construída, pois o item inicial em Spinner é considerado
     * um "novo item selecionado", dessa forma desde o início
     * a TrueTime API será solicitada sem que nós
     * desenvolvedores tenhamos de criar algum código somente
     * para essa invocação inicial.
     * */
    override fun onItemSelected(
            adapter: AdapterView<*>?,
            itemView: View?,
            position: Int,
            id: Long) {

        /*
         * O array countriesGmt facilita o acesso ao formato GMT
         * String esperado em TimeZone.getTimeZone(), assim não
         * há necessidade de blocos condicionais ou expressões
         * regulares para ter acesso ao GMT correto de acordo
         * com o item escolhido.
         * */
        AsyncTrueTime(this)
                .execute(countriesGmt[position])
    }

    override fun onNothingSelected(adapter: AdapterView<*>?) {}

    /**
     * Método responsável por apresentar / esconder a View de informação
     * sobre a origem do horário (GMT) sendo utilizado: servidor NTP
     * (certeza que o horário estará correto); ou aparelho. Este método
     * será invocado sempre no doInBackground() de uma instância de
     * AsyncTrueTime, por isso a necessidade do runOnUiThread() para que
     * a atualização de View não seja fora da Thread UI.
     * */
    fun infoDateShow(status: Boolean) {

        runOnUiThread {
            ll_info_date.visibility =
                    if (status) /* Origem: aparelho */
                        View.VISIBLE
                    else /* Origem: servidor NTP */
                        View.INVISIBLE
        }
    }

    /*
     * Método responsável por atualizar tanto o ClockImageView
     * quanto o TextView de horário de acordo com o parâmetro
     * Calendar fornecido. Este método será invocado sempre no
     * onPostExecute() de uma instância de AsyncTrueTime.
     * */
    fun updateClock(trueTime: Calendar) {

        val hour = trueTime.get(Calendar.HOUR_OF_DAY)
        val minute = trueTime.get(Calendar.MINUTE)

        /*
         * Atualizando o ClockImageView com aplicação de animação.
         * */
        civ_clock.animateToTime(hour, minute)

        /*
         * O formato "%02d:%02d" garante que em hora e em minuto não
         * haverá números menores do que 10 não acompanhados de um 0
         * a esquerda.
         * */
        tv_clock.text = String.format("%02d:%02d", hour, minute)
        lottieContainer.updateByHour(hour)
    }

    /*
     * Método que invocará onItemSelected() para atualizar o
     * horário, isso, pois fireSpinnerItemSelected() somente
     * será acionado assim que a API TrueTime tiver retorno de
     * algum servidor NTP. fireSpinnerItemSelected() garante
     * que o horário em apresentação é o correto.
     * */
    fun fireSpinnerItemSelected() {
        sp_countries.onItemSelectedListener
                .onItemSelected(null, null, sp_countries.selectedItemPosition, 0)
    }

    fun setNewColors(isMorning: Boolean) {
        if (isMorning) {
            sp_countries.setPopupBackgroundResource(R.color.colorSunSky)
            chanceColorStatusBar(R.color.colorSunSky)
        } else {
            sp_countries.setPopupBackgroundResource(R.color.colorMoonSky)
            chanceColorStatusBar(R.color.colorMoonSky)
        }
    }

    private fun chanceColorStatusBar(color: Int) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = this.resources.getColor(color)
        }
    }
}
