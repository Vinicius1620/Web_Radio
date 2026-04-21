package com.example.webradio

import androidx.core.content.ContextCompat
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProgramacaoActivity : AppCompatActivity() {

    data class Programa(val horario: String, val nome: String, val descricao: String, val emoji: String)

    val programas = mapOf(
        "SEG" to listOf(
            Programa("06:00", "A Voz do Pastor", "Pr. Francisco Cícero Miranda", "🎙️"),
            Programa("06:20", "Louvor do Sertão", "Louvores", "🎵"),
            Programa("07:00", "Família Debaixo da Graça", "Sara, Renato, Renildo e Fátima", "👨‍👩‍👧"),
            Programa("08:00", "Viva Feliz", "Talita Mikaelly", "😊"),
            Programa("11:00", "Mulheres de Fé", "Alexandra Nascimento", "🙏"),
            Programa("12:00", "Palavra de Sabedoria", "Pastor Wendel", "📖"),
            Programa("13:00", "Tarde Mix", "Músicas", "🎶"),
            Programa("18:00", "Conexão Gospel", "Hinos", "📻"),
            Programa("00:00", "Palavra de Sabedoria", "Pastor Wendel", "📖")
        ),
        "TER" to listOf(
            Programa("06:00", "A Voz do Pastor", "Pr. Francisco Cícero Miranda", "🎙️"),
            Programa("06:20", "Louvor do Sertão", "Louvores", "🎵"),
            Programa("07:00", "Família Debaixo da Graça", "Sara, Renato, Renildo e Fátima", "👨‍👩‍👧"),
            Programa("08:00", "Viva Feliz", "Talita Mikaelly", "😊"),
            Programa("11:00", "Mulheres de Fé", "Alexandra Nascimento", "🙏"),
            Programa("12:00", "Palavra de Sabedoria", "Pastor Wendel", "📖"),
            Programa("13:00", "Tarde Mix", "Músicas", "🎶"),
            Programa("18:00", "Conexão Gospel", "Hinos", "📻"),
            Programa("23:00", "Pão da Vida", "Pastor Josimar", "🌙"),
            Programa("00:00", "Palavra de Sabedoria", "Pastor Wendel", "📖")
        ),
        "QUA" to listOf(
            Programa("06:00", "A Voz do Pastor", "Pr. Francisco Cícero Miranda", "🎙️"),
            Programa("06:20", "Louvor do Sertão", "Louvores", "🎵"),
            Programa("07:00", "Família Debaixo da Graça", "Sara, Renato, Renildo e Fátima", "👨‍👩‍👧"),
            Programa("08:00", "Viva Feliz", "Talita Mikaelly", "😊"),
            Programa("11:00", "Mulheres de Fé", "Alexandra Nascimento", "🙏"),
            Programa("12:00", "Palavra de Sabedoria", "Pastor Wendel", "📖"),
            Programa("13:00", "Tarde Mix", "Músicas", "🎶"),
            Programa("19:00", "Culto da Restauração", "AD Baraúna - Sede", "⛪"),
            Programa("00:00", "Palavra de Sabedoria", "Pastor Wendel", "📖")
        ),
        "QUI" to listOf(
            Programa("06:00", "A Voz do Pastor", "Pr. Francisco Cícero Miranda", "🎙️"),
            Programa("06:20", "Louvor do Sertão", "Louvores", "🎵"),
            Programa("07:00", "Família Debaixo da Graça", "Sara, Renato, Renildo e Fátima", "👨‍👩‍👧"),
            Programa("08:00", "Viva Feliz", "Talita Mikaelly", "😊"),
            Programa("11:00", "Mulheres de Fé", "Alexandra Nascimento", "🙏"),
            Programa("12:00", "Palavra de Sabedoria", "Pastor Wendel", "📖"),
            Programa("13:00", "Tarde Mix", "Músicas", "🎶"),
            Programa("18:00", "Conexão Gospel", "Hinos", "📻"),
            Programa("00:00", "Palavra de Sabedoria", "Pastor Wendel", "📖")
        ),
        "SEX" to listOf(
            Programa("06:00", "A Voz do Pastor", "Pr. Francisco Cícero Miranda", "🎙️"),
            Programa("06:20", "Louvor do Sertão", "Louvores", "🎵"),
            Programa("07:00", "Família Debaixo da Graça", "Sara, Renato, Renildo e Fátima", "👨‍👩‍👧"),
            Programa("08:00", "Viva Feliz", "Talita Mikaelly", "😊"),
            Programa("11:00", "Mulheres de Fé", "Alexandra Nascimento", "🙏"),
            Programa("12:00", "Palavra de Sabedoria", "Pastor Wendel", "📖"),
            Programa("13:00", "Tarde Mix", "Músicas", "🎶"),
            Programa("18:00", "Conexão Gospel", "Hinos", "📻"),
            Programa("00:00", "Palavra de Sabedoria", "Pastor Wendel", "📖")
        ),
        "SÁB" to listOf(
            Programa("06:00", "A Voz do Pastor", "Pr. Francisco Cícero Miranda", "🎙️"),
            Programa("10:00", "Portal da Fé", "Aux. Pedro e Diácono Hermínio", "✝️"),
            Programa("12:00", "Palavra de Sabedoria", "Pastor Wendel", "📖"),
            Programa("13:00", "Tarde Mix", "Músicas", "🎶"),
            Programa("16:00", "Palavra de Fé", "Mensagem de fé", "🙏"),
            Programa("18:00", "Conexão Gospel", "Hinos", "📻"),
            Programa("00:00", "Palavra de Sabedoria", "Pastor Wendel", "📖")
        ),
        "DOM" to listOf(
            Programa("06:00", "A Voz do Pastor", "Pr. Francisco Cícero Miranda", "🎙️"),
            Programa("12:00", "Palavra de Sabedoria", "Pastor Wendel", "📖"),
            Programa("13:00", "Tarde Mix", "Músicas", "🎶"),
            Programa("18:00", "Conexão Gospel", "Hinos", "📻"),
            Programa("00:00", "Palavra de Sabedoria", "Pastor Wendel", "📖")
        )
    )

    val dias = listOf("SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM")
    var diaAtivo = "SEG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programacao)


        val viewPager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)

        val btnIds = mapOf(
            "SEG" to R.id.btnSeg,
            "TER" to R.id.btnTer,
            "QUA" to R.id.btnQua,
            "QUI" to R.id.btnQui,
            "SEX" to R.id.btnSex,
            "SÁB" to R.id.btnSab,
            "DOM" to R.id.btnDom
        )

        val adapter = ProgramacaoAdapter(dias, programas, viewPager, btnIds, this)
        viewPager.adapter = adapter


        // A função abaixo Descobre que dia é hoje, acha esse dia na lista e abre a tela já mostrando a programação de hoje com a aba correta selecionada.

        // Vai direto para o dia de hoje
        //A linha abaixo pega o calendario do celular no extato momento - data e hora atual
        val calendar = java.util.Calendar.getInstance()
        //A linha abaixo verifica qual é o dia da semana atual e converte para sigla que usar no app
        val diaSemana = when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY    -> "SEG"
            java.util.Calendar.TUESDAY   -> "TER"
            java.util.Calendar.WEDNESDAY -> "QUA"
            java.util.Calendar.THURSDAY  -> "QUI"
            java.util.Calendar.FRIDAY    -> "SEX"
            java.util.Calendar.SATURDAY  -> "SÁB"
            java.util.Calendar.SUNDAY    -> "DOM"
            else                         -> ""
        }
        //A linha abaixo pega a lista de dias ( Acha a posição do dia atual)
        val indexHoje = dias.indexOf(diaSemana)
        // false = sem animação ao abrir (Manda o ViewPager abrir já na aba do dia de hoje, sem animação de deslize (false))
        viewPager.setCurrentItem(indexHoje, false)
        // marca o dia atual ccomo dia ativo
        diaAtivo = diaSemana
        atualizarAbas(btnIds)

        // Sincroniza abas com o ViewPager
        viewPager.registerOnPageChangeCallback(object :
            androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                diaAtivo = dias[position]
                atualizarAbas(btnIds)
            }
        })

        // Clique nas abas troca a página
        dias.forEach { dia ->
            findViewById<TextView>(btnIds[dia]!!).setOnClickListener {
                diaAtivo = dia
                viewPager.currentItem = dias.indexOf(dia)
                atualizarAbas(btnIds)
            }
        }

        // A linha abaixo atualza visualmente os botões das abas e fica com botão destacado
        atualizarAbas(btnIds)
    }

    private fun atualizarAbas(btnIds: Map<String, Int>) {
        dias.forEach { dia ->
            val btn = findViewById<TextView>(btnIds[dia]!!)
            if (dia == diaAtivo) {
                btn.setTextColor(Color.WHITE)
                btn.background = ContextCompat.getDrawable(this, R.drawable.tab_ativa)
            } else {
                btn.setTextColor(Color.parseColor("#aaaaaa"))
                btn.background = ContextCompat.getDrawable(this, R.drawable.tab_inativa)
            }
        }
    }

    private fun carregarProgramas(dia: String) {
        val layout = findViewById<LinearLayout>(R.id.layoutProgramasDia)
        layout.removeAllViews()

        programas[dia]?.forEach { programa ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                //A LINHA ABAXO ALTERAR A COR DAS BARRA DA PROGRAMAÇÃO
                background = ContextCompat.getDrawable(context, R.drawable.card_background)
                setPadding(16, 16, 16, 16)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 12)
                layoutParams = params
            }

            // Emoji
            val emoji = TextView(this).apply {
                text = programa.emoji
                textSize = 28f
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 16, 0)
                layoutParams = params
            }

            // Textos
            val textos = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            //A LINHA ABAXO E PRA HORARIO DA PROGRAMAÇÃO
            val horario = TextView(this).apply {
                text = programa.horario
                setTextColor(Color.parseColor("#FFFFFF"))
                textSize = 13f
            }

            val nome = TextView(this).apply {
                text = programa.nome
                setTextColor(Color.WHITE)
                textSize = 16f
                setPadding(0, 4, 0, 0)
            }

            //A LINHA ABAXO E PRA DESCRICAO DO PROGRAMA
            val descricao = TextView(this).apply {
                text = programa.descricao
                setTextColor(Color.parseColor("#aaaaaa"))
                textSize = 13f
            }

            textos.addView(horario)
            textos.addView(nome)
            textos.addView(descricao)
            card.addView(emoji)
            card.addView(textos)
            layout.addView(card)
        }
    }
}