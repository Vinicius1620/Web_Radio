package com.example.webradio

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class ProgramacaoAdapter(
    private val dias: List<String>,
    private val programas: Map<String, List<ProgramacaoActivity.Programa>>,
    private val viewPager: ViewPager2,
    private val btnIds: Map<String, Int>,
    private val activity: ProgramacaoActivity
) : RecyclerView.Adapter<ProgramacaoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout = view.findViewById(R.id.layoutProgramasDia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_programacao_dia, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //A LINHA ABAIXO PEGA O DIA DAS ABAS ATUAL
        val dia = dias[position]
        holder.layout.removeAllViews()

        // PEGA A HORA ATUAL
        val calendar = java.util.Calendar.getInstance()
        val horaAtual = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minutoAtual = calendar.get(java.util.Calendar.MINUTE)
        val totalMinutosAgora = horaAtual * 60 + minutoAtual

        // Dia de hoje para comparar
        // SABER QUE DIA É HOJE, NO FORMATO DO APP
        val diaSemanaHoje = when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY    -> "SEG"
            java.util.Calendar.TUESDAY   -> "TER"
            java.util.Calendar.WEDNESDAY -> "QUA"
            java.util.Calendar.THURSDAY  -> "QUI"
            java.util.Calendar.FRIDAY    -> "SEX"
            java.util.Calendar.SATURDAY  -> "SÁB"
            else                         -> "DOM"
        }


        val listaDia = programas[dia] ?: return

        // Acha qual programa está no ar agora (só se for o dia de hoje)
        var programaAoVivo: ProgramacaoActivity.Programa? = null

        //ESSE BLOCO E PARA FAZER TESTE SOBRE A PROGRAMAÇÃO DE MAIA-NOITE
        for (i in listaDia.indices) {
            val (h, m) = listaDia[i].horario.split(":").map { it.toInt() }

            // 00:00 = fim do dia (1440 min)
            val inicioPrograma = if (h == 0 && m == 0) 24 * 60 else h * 60 + m

            val fimPrograma = if (i + 1 < listaDia.size) {
                val (hP, mP) = listaDia[i + 1].horario.split(":").map { it.toInt() }
                if (hP == 0 && mP == 0) 24 * 60 else hP * 60 + mP
            } else {
                24 * 60
            }

            if (totalMinutosAgora >= inicioPrograma && totalMinutosAgora < fimPrograma) {
                programaAoVivo = listaDia[i]
                break
            }

            // Se passou de todos, pega o penúltimo (último antes do 00:00)
            if (i == listaDia.size - 2 && programaAoVivo == null) {
                if (totalMinutosAgora >= inicioPrograma) {
                    programaAoVivo = listaDia[i]
                    break
                }
            }
        }


        //ESSE BLOCO DE CODICO COMENTADO FOI ALTERADO POR ESSE DE CIMA PARA TESTE DE SINCRONIZAÇÃO DE NOTIFICAÇÃO E APP
        /*if (dia == diaSemanaHoje) {
            for (i in listaDia.indices) {
                val (h, m) = listaDia[i].horario.split(":").map { it.toInt() }
                val inicioPrograma = h * 60 + m
                val fimPrograma = if (i + 1 < listaDia.size) {
                    val (hP, mP) = listaDia[i + 1].horario.split(":").map { it.toInt() }
                    hP * 60 + mP
                } else {
                    24 * 60 // último programa vai até meia-noite
                }
                if (totalMinutosAgora in inicioPrograma until fimPrograma) {
                    programaAoVivo = listaDia[i]
                    break
                }
            }
        }*/

        //E RESPONSAVEL POR DESNHA O CARD DE CADA PROGRAMAÇÃO
        listaDia.forEach { programa ->
            val eAoVivo = programa == programaAoVivo

            val card = LinearLayout(holder.itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    // 👇 card destacado para o programa ao vivo
                    if (eAoVivo) R.drawable.card_ao_vivo else R.drawable.card_background
                )
                setPadding(16, 16, 16, 16)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 12)
                layoutParams = params
            }

            val emoji = TextView(holder.itemView.context).apply {
                text = programa.emoji
                textSize = 28f
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 16, 0)
                layoutParams = params
            }

            val textos = LinearLayout(holder.itemView.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            //A LINHA ABAIXO VAI PARA O CARD DE CADA PROGRAMA QUANDO ESTA NO AR
            val horario = TextView(holder.itemView.context).apply {
                text = if (eAoVivo) "🔴 AO VIVO  •  ${programa.horario}" else programa.horario
                setTextColor(if (eAoVivo) Color.WHITE else Color.parseColor("#ef7d00"))
                textSize = 13f
            }

            val nome = TextView(holder.itemView.context).apply {
                text = programa.nome
                setTextColor(Color.WHITE)
                textSize = if (eAoVivo) 17f else 16f
                setPadding(0, 4, 0, 0)
            }

            val descricao = TextView(holder.itemView.context).apply {
                text = programa.descricao
                setTextColor(if (eAoVivo) Color.parseColor("#dddddd") else Color.parseColor("#aaaaaa"))
                textSize = 13f
            }

            textos.addView(horario)
            textos.addView(nome)
            textos.addView(descricao)
            card.addView(emoji)
            card.addView(textos)
            holder.layout.addView(card)
        }
    }

    override fun getItemCount() = dias.size
}