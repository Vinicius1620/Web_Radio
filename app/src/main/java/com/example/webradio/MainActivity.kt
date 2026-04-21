package com.example.webradio



import android.widget.ImageButton
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private var tocando = false

    // ⚠️ Coloque aqui a URL do stream da sua rádio
    // URL DO SERVIDOR
    private val streamUrl = "https://painel.streamcasthost.com/listen/web_radio_maranata/radio.mp3"


    private lateinit var radioReceiver: android.content.BroadcastReceiver
    //A LINHA ABAIXO E REFERENTE A BroadcastReceiver do btnPlayPause
    private lateinit var btnPlayPause: ImageButton



    // ESSE BLOCO VAI ATUALIZA O QUE ESTA SENDO NA PROGRAMAÇÃO DA RADIO
    //O código pega o horário atual do celular e compara com os horários da programação
    //Faz a Conversão de tudo em minutos para facilitar a comparação
    //Exemplo:
    //08:30 → 8 * 60 + 30 = 510 minutos
    private fun getProgramaAtual(): String {
        val calendar = java.util.Calendar.getInstance()
        val hora = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minuto = calendar.get(java.util.Calendar.MINUTE)
        val horaAtual = hora * 60 + minuto

        // ESSA LINHA ABAIXO FAZ A COMPARAÇÃO DA HORA DO CELULA QUE ESTA o APLICATIVO
        // ESSE BOLOCO É RESPONSAVEL POR TEXTO NA PARTE DE BAIXO DA LOGO E NOME DA RADIO
        val diaSemana = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)


        return when {

            horaAtual >= 6 * 60 && horaAtual < 6 * 60 + 20 -> "Ao vivo: A Voz do Pastor"
            horaAtual >= 6 * 60 + 20 && horaAtual < 7 * 60 -> "Ao vivo: Louvor do Sertão"
            horaAtual >= 7 * 60 && horaAtual < 8 * 60 -> "Ao vivo: Família Debaixo da Graça"
            horaAtual >= 8 * 60 && horaAtual < 11 * 60 -> "Ao vivo: Viva Feliz"
            horaAtual >= 11 * 60 && horaAtual < 12 * 60 -> "Ao vivo: Mulheres de Fé"
            horaAtual >= 12 * 60 && horaAtual < 13 * 60 -> "Ao vivo: Palavra de Sabedoria"
            horaAtual >= 13 * 60 && horaAtual < 18 * 60 -> "Ao vivo: Trade Mix"
            horaAtual >= 18 * 60 && horaAtual < 23 * 60 -> "Ao vivo: Conexão Gospel"
            horaAtual >= 23 * 60 -> "Ao vivo: Palavra de Sabedoria"

            else -> "🔴 Ao vivo agora!"
        }
    }

    /*return when {
        horaAtual >= 6 * 60 && horaAtual < 8 * 60 -> "Ao vivo Manhã com Deus"
        horaAtual >= 8 * 60 && horaAtual < 10 * 60 -> "Ao vivo Palavra da Vida"
        horaAtual >= 10 * 60 && horaAtual < 12 * 60 -> "Ao vivo Gospel Mix"
        horaAtual >= 12 * 60 && horaAtual < 14 * 60 -> "Ao vivo Hora do Almoço"
        horaAtual >= 14 * 60 && horaAtual < 16 * 60 -> "Ao vivo Tarde de Graça"
        horaAtual >= 16 * 60 && horaAtual < 18 * 60 -> "Ao vivo Voz da Verdade"
        horaAtual >= 18 * 60 && horaAtual < 20 * 60 -> "Ao vivo Conexão Gospel"
        horaAtual >= 20 * 60 -> "Ao vivo Vigília da Fé"
        else -> "🔴 Ao vivo agora!"
    }
}*/

    private fun pedirPermissaoNotificacao() {
        // Só necessário no Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    //ESSE BLOQUE ORIENTA AO USUARIO LIBERAR AS NOTIFICAÇÕES
    private fun mostrarDicaNotificacao() {
        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val jaExibiu = prefs.getBoolean("dica_bloqueio", false)

        if (!jaExibiu) {
            android.app.AlertDialog.Builder(this)
                .setTitle("🔒 Notificação na tela de bloqueio")
                .setMessage(
                    "Para ver os controles da rádio na tela de bloqueio:\n\n" +
                            "1. Vá em Configurações\n" +
                            "2. Apps → Web Rádio\n" +
                            "3. Notificações → ative 'Na tela de bloqueio'"
                )
                .setPositiveButton("Entendi") { dialog, _ -> dialog.dismiss() }
                .show()

            prefs.edit().putBoolean("dica_bloqueio", true).apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Reseta o ícone para play ao abrir o app
        val prefs = getSharedPreferences("radio_state", MODE_PRIVATE)
        prefs.edit().putBoolean("tocando", false).apply()

        pedirPermissaoNotificacao()
        mostrarDicaNotificacao()

        // SafeArea — respeita barra de status e navegação
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val rootView = findViewById<android.view.View>(R.id.root)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setContentView(R.layout.activity_main)

        //ESSA LINHA FOI ADICIONADA PARA SICRONIZAR A BARRA DE NOTIFICAÇÃO COM O ESTADO REAL DO APLICATIVO
        // Recebe atualizações do RadioService
        radioReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: Intent?) {
                val tocandoAgora = intent?.getBooleanExtra("tocando", false) ?: false
                tocando = tocandoAgora
                if (tocandoAgora) {
                    btnPlayPause.setImageResource(R.mipmap.pausa_musica)
                } else {
                    btnPlayPause.setImageResource(R.mipmap.botao_play)
                }
            }
        }


        //A LINHA BAIXO VAI PARA OPÇÃO DO (MENU)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)

        btnMenu.setOnClickListener {
            val popup = android.widget.PopupMenu(this, btnMenu)
            popup.menu.add("📻 Sobre-nos")
            popup.menu.add("📍 Onde Estamos")
            popup.menu.add("⭐ Avaliar")
            popup.menu.add("❓ Ajudar")
            popup.menu.add("Politica de Privacidade")
            popup.menu.add("Termos de Uso")
            popup.menu.add("💻 Informações do Sistema")


            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "❓ Ajudar" -> {
                        val builder = android.app.AlertDialog.Builder(this)
                        builder.setTitle("Ajudar")
                        builder.setMessage(
                            "🆘 Sistema de Rádio Web\n\n" +
                                    "👨‍💻 Desenvolvido por\n" +
                                    "Adson Vinicius\n\n" +
                                    "Entre em contato pelo Whatsapp\n\n" +
                                    "(84) 99212-9388\n"
                        )
                        builder.setPositiveButton("Fechar") { dialog, _ ->
                            dialog.dismiss()
                        }
                        builder.show()
                    }
                }

                when (item.title) {
                    "💻 Informações do Sistema" -> {
                        val builder = android.app.AlertDialog.Builder(this)
                        builder.setTitle("Informações do Sistema")
                        builder.setMessage(
                            "📱 Rádio Web Maranata\n\n" +
                                    "Plataforma digital para streaming de áudio de alta qualidade.\n\n" +
                                    "Desenvolvimento:\n\n" +
                                    "Este sistema foi projetado e desenvolvido pela Ij Desenvolvimento de Software\n\n"
                        )
                        builder.setPositiveButton("Fechar") { dialog, _ ->
                            dialog.dismiss()
                        }
                        builder.show()
                    }
                }

                when (item.title) {
                    "📍 Onde Estamos" -> {
                        try {
                            val uri = Uri.parse("https://maps.app.goo.gl/zAY7PKBBX5MtVWti9")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                        } catch (e: Exception) {
                            // Se não tiver Maps abre no navegador
                            val url = "https://maps.app.goo.gl/zAY7PKBBX5MtVWti9"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        }
                    }
                }

                when (item.title) {
                    "⭐ Avaliar" -> {
                        val uri = Uri.parse("market://details?id=com.example.webradio")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            val webIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=com.example.webradio")
                            )
                            startActivity(webIntent)
                        }
                    }
                }

                when (item.title) {
                    "Politica de Privacidade" -> {
                        val url = "https://doc-hosting.flycricket.io/web-radio-privacy-policy/93fc2678-ca88-4300-80ae-402080fb42c9/privacy"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    }
                }

                when (item.title) {
                    "Termos de Uso" -> {
                        val url = "https://doc-hosting.flycricket.io/web-radio-terms-of-use/63f05a69-719e-4813-82af-75ad1f1bc969/terms"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    }
                }

                when (item.title) {
                    "📻 Sobre-nos" -> {
                        val builder = android.app.AlertDialog.Builder(this)
                        builder.setTitle("Sobre")
                        builder.setMessage(
                            "A Voz da Assembleia de Deus em Baraúna/RN\n\n" +
                                    "📻 Músicas gospel 24 horas!\n" +
                                    "🙏 Transmitindo fé e esperança!\n\n" +
                                    "Uma Igreja Missionária — Desde 1964.\n\n" +
                                    "© Direitos Reservados — Assembleia de Deus no Rio Grande do Norte\n" +
                                    "Direção Geral: Assembleia de Deus AD Baraúna/RN\n"
                        )
                        builder.setPositiveButton("Fechar") { dialog, _ ->
                            dialog.dismiss()
                        }
                        builder.show()
                    }
                }
                true
            }
            popup.show()
        }

        //A LINHA ABAIXO VAI PARA A PROGRAMAÇÃO DA RADIO
        val btnProgramacao = findViewById<Button>(R.id.btnProgramacao)
        btnProgramacao.setOnClickListener {
            val intent = Intent(this, ProgramacaoActivity::class.java)
            startActivity(intent)
        }


        //A LINHA ABAIXO É DO ICONE DE COMPARTILHAMENTO
        val btnCompartilhar = findViewById<ImageButton>(R.id.btnCompartilhar)

        btnCompartilhar.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Rádio Web AD Baraúna")
            intent.putExtra(Intent.EXTRA_TEXT,
                "🔔 Sintonize na Fé!\n\n" +
                        "Já baixou o app da AD Baraúna? Não perca um minuto da nossa programação especial.\n\n" +
                        "🎧 Ouça agora:\n" +
                        //A LINHA ABAXO É PARA O LINK DA PLAY STORY DO APLICATIVO
                        "https://play.google.com/store/apps/details?id=com.example.webradio")
            startActivity(Intent.createChooser(intent, "Compartilhar via"))
        }


        //A LINHA ABAXO FAZ O DIRECIONAMENTO PARA REDE SOCIAIS (INSTAGRAM)
        val btnInstagram = findViewById<LinearLayout>(R.id.btnInstagram)
        btnInstagram.setOnClickListener {
            val url = "https://www.instagram.com/ad_baraunarn"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        val btnYoutube = findViewById<LinearLayout>(R.id.btnYoutube)
        btnYoutube.setOnClickListener {
            val url = "http://www.youtube.com/@ADBara%C3%BAna-RN"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        val btnPlayPause = findViewById< ImageButton>(R.id.btnPlayPause)
        val tvMusica = findViewById<TextView>(R.id.tvMusica)
        tvMusica.text = getProgramaAtual()

        // Inicializa o player
        player = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(streamUrl)
        player.setMediaItem(mediaItem)
        player.prepare()

        // Botão Play/Pause
        btnPlayPause.setOnClickListener {
            if (tocando) {
                val intent = Intent(this, RadioService::class.java)
                intent.action = "PAUSE"
                startService(intent)
                //A LINHA ABAIXO FAZ A TROCA DOS ICONE DE PLAY
                btnPlayPause.setImageResource(R.mipmap.botao_play)
                tocando = false
            } else {
                val intent = Intent(this, RadioService::class.java)
                intent.action = "PLAY"
                startService(intent)
                //A LINHA ABAIXO FAZ A TROCA DOS ICONE DE PAUSE
                btnPlayPause.setImageResource(R.mipmap.pausa_musica)
                tocando = true
            }
        }
    }

    //ESSA LINHA FOI ADICIONADA PARA SICRONIZAR A BARRA DE NOTIFICAÇÃO COM O ESTADO REAL DO APLICATIVO
    override fun onResume() {
        super.onResume()
        androidx.localbroadcastmanager.content.LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(radioReceiver,
                android.content.IntentFilter("RADIO_STATE_CHANGED"))

        //LÊ O ESTADO SALVO AO VOLTAR PARA O APLICATIVO
        val prefs = getSharedPreferences("radio_state", MODE_PRIVATE)
        tocando = prefs.getBoolean("tocando", false)
        btnPlayPause = findViewById<ImageButton>(R.id.btnPlayPause)
        if (tocando) {
            btnPlayPause.setImageResource(R.mipmap.pausa_musica)
        } else {
            btnPlayPause.setImageResource(R.mipmap.botao_play)
        }
    }


    override fun onPause() {
        super.onPause()
        androidx.localbroadcastmanager.content.LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(radioReceiver)
    }



    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}