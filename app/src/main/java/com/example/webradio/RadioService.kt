package com.example.webradio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import android.graphics.Color
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.content.ContextCompat

class RadioService : Service() {

    private lateinit var player: ExoPlayer

    //URL DO SERVIDOR DA RÁDIO
    private val streamUrl = "https://painel.streamcasthost.com/listen/web_radio_maranata/radio.mp3"
    private val CHANNEL_ID = "radio_channel"
    private val NOTIFICATION_ID = 1
    private lateinit var audioManager: AudioManager
    private var focusRequest: AudioFocusRequest? = null

    private var pausadoManualmente = false

    //ESSA VARIAVEL E RESPONSAVEL POR BARRA DE NOTIFICAÇÃO NA TELA DE BLOQUIO
    private lateinit var mediaSession: MediaSessionCompat

    override fun onCreate() {
        super.onCreate()

        // INICIALIZA O MEDIASESSION PARA A BARRA DE NOTIFICAÇÃO NA TELA DE BLOQUEIO
        mediaSession = MediaSessionCompat(this, "RadioSession")
        mediaSession.isActive = true


        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                pausadoManualmente = false
                player.play()
                atualizarNotificacao(tocando = true)
            }

            override fun onPause() {
                pausadoManualmente = true
                player.pause()
                atualizarNotificacao(tocando = false)
            }

            override fun onStop() {
                player.stop()
                stopSelf()
            }
        })

        //🔊 Audio Focus — Abaixar volume automaticamente
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            //a linha abaixo é para ouvir o audio
                            player.volume = 0.5f
                            if (!pausadoManualmente) {
                                player.play()
                                atualizarNotificacao(tocando = true)
                            }
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            // Pausa durante gravação de vídeo no WhatsApp ou app nativo
                            player.pause()
                            atualizarNotificacao(tocando = false)
                        }

                        // LINHA DE BLOCO FOI COMENTADA PARA TESTE DE SINCRONIZAÇÃO DE NOTIFICAÇÃO E APP

                        /*AudioManager.AUDIOFOCUS_GAIN -> {
                            player.volume = 1.0f  // volta volume ao normal
                            if (!pausadoManualmente) {
                                player.play()
                                atualizarNotificacao(tocando = true)
                            }
                        }*/
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                            // Pausa — gravação de vídeo ou ligação
                            player.pause()
                            atualizarNotificacao(tocando = false)
                        }
                        AudioManager.AUDIOFOCUS_LOSS -> {
                            // Pausa permanente
                            if (!pausadoManualmente) {
                                player.pause()
                                atualizarNotificacao(tocando = false)
                            }
                        }
                    }
                }
                .build()
            audioManager.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            player.volume = 1.0f
                            player.play()
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            player.volume = 0.2f
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                            player.pause()
                        }
                        AudioManager.AUDIOFOCUS_LOSS -> {
                            player.pause()
                        }
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        player = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(streamUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        // LINHA ABAIXO FOI COMENTADA PARA TESTE DE SINCRONIZAÇÃO DE NOTIFICAÇÃO E APP
        //player.play()

        //player.play()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(1, criarNotificacao(tocando = false))

        when (intent?.action) {
            "PLAY" -> {
                pausadoManualmente = false
                player.play()
                atualizarNotificacao(tocando = true)
            }
            "PAUSE" -> {
                pausadoManualmente = true
                player.pause()
                atualizarNotificacao(tocando = false)
            }
            else -> {
                return START_STICKY
            }
        }
        //startForeground(1, criarNotificacao(tocando = false))
        return START_STICKY
    }

    //ESSA FUNÇÃO SERVE PARA REMOVE O APP DA LISTA DE RECENTES APLICATIVOS
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        player.stop()
        stopSelf()
    }


    // ESSA FUNÇÃO E RESPONSAVEL PELA BARRA DE NOTIFICAÇÃO DO APLICATIVO
    private fun criarNotificacao(tocando: Boolean = false): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Rádio Web",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        // Atualiza metadados — título e arte da notificação
        val arte = ContextCompat.getDrawable(this, R.mipmap.icone_aplicativo_redondo)?.toBitmap()
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Web Rádio Maranata")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, if (tocando) "🔴 Ao vivo agora!" else "⏸ Pausado")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, arte)
                .build()
        )

        // Atualiza estado de playback
        val state = if (tocando) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, 0, 1f)
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_STOP
                )
                .build()
        )

        val abrirApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val pendingPlay = PendingIntent.getService(
            this, 1,
            Intent(this, RadioService::class.java).apply { action = "PLAY" },
            PendingIntent.FLAG_IMMUTABLE
        )

        val pendingPause = PendingIntent.getService(
            this, 2,
            Intent(this, RadioService::class.java).apply { action = "PAUSE" },
            PendingIntent.FLAG_IMMUTABLE
        )

        val pendingStop = PendingIntent.getService(
            this, 3,
            Intent(this, RadioService::class.java).apply { action = "STOP" },
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Web Rádio Maranata")
            .setContentText(if (tocando) "🔴 Tocando ao vivo!" else "⏸ Pausado")
            .setSmallIcon(R.mipmap.icone_aplicativo_redondo)
            // 👇 Arte grande na notificação e tela de bloqueio
            .setLargeIcon(arte)
            .setContentIntent(abrirApp)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setColor(Color.parseColor("#505E78"))
            // 👇 Vincula ao MediaSession — isso ativa o controle rico na tela de bloqueio
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1)
            )

        if (tocando) {
            builder.addAction(R.drawable.ic_pause, "Pausar", pendingPause)
        } else {
            builder.addAction(R.drawable.ic_stop, "Parar", pendingStop)
        }
        builder.addAction(R.drawable.ic_play, "Play", pendingPlay)

        return builder.build()
    }

    private fun atualizarNotificacao(tocando: Boolean) {
        //ESSA LINHA FOI ADICIONADA PARA SICRONIZAR A BARRA DE NOTIFICAÇÃO COM O ESTADO REAL DO APLICATIVO
        val prefs = getSharedPreferences("radio_state", MODE_PRIVATE)
        prefs.edit().putBoolean("tocando", tocando).apply()

        // DEPOIS
        val intent = Intent("RADIO_STATE_CHANGED")
        intent.putExtra("tocando", tocando)
        androidx.localbroadcastmanager.content.LocalBroadcastManager
            .getInstance(this)
            .sendBroadcast(intent)

        /*
        // Avisa o MainActivity em tempo real
        val intent = Intent("RADIO_STATE_CHANGED")
        intent.putExtra("tocando", tocando)
        sendBroadcast(intent)*/


        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, criarNotificacao(tocando))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        // Reseta o estado ao fechar o app
        val prefs = getSharedPreferences("radio_state", MODE_PRIVATE)
        prefs.edit().putBoolean("tocando", false).apply()

        mediaSession.release()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        }
        player.release()
    }
}