package uz.doniyorbek7376.jukebox

import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import java.util.*

class JukeBox: AbstractVerticle() {
  private enum class State {PLAYING, PAUSED}
  private var currentState:State = State.PAUSED
  private val playlist:Queue<String> = ArrayDeque()
  private val logger = LoggerFactory.getLogger(JukeBox::class.java)

  override fun start() {
    val eventBus = vertx.eventBus()
    eventBus.consumer("jukebox.list", this::list)
    eventBus.consumer("jukebox.schedule", this::schedule)
    eventBus.consumer("jukebox.play", this::play)
    eventBus.consumer("jukebox.pause", this::pause)
    vertx.createHttpServer()
      .requestHandler(this::httpHandler)
      .listen(8080)
    vertx.setPeriodic(100, this::streamAudioChunk)
  }

  private fun list(request: Message<Any>) {
    TODO("Implement")
  }
  private fun schedule(request: Message<JsonObject>) {
    val fileName = request.body().getString("file")
    if(playlist.isEmpty() && currentState == State.PAUSED) {
      currentState = State.PLAYING
    }
    playlist.offer(fileName)
  }
  private fun play(request: Message<Any>) {
    currentState = State.PLAYING
  }

  private fun pause(request: Message<Any>) {
    currentState = State.PAUSED
  }
  private fun httpHandler(request: HttpServerRequest) {
    TODO("Implement")
  }
  private fun streamAudioChunk(id:Long) {
    TODO("Implement")
  }
}
