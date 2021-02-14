package uz.doniyorbek7376.jukebox

import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.io.File
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashSet

class JukeBox : AbstractVerticle() {
  private enum class State { PLAYING, PAUSED }

  private var currentState: State = State.PAUSED
  private val playlist: Queue<String> = ArrayDeque()
  private val logger = LoggerFactory.getLogger(JukeBox::class.java)
  private val streamers:MutableSet<HttpServerResponse> = HashSet()

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
    vertx.fileSystem().readDir("tracks", ".*mp3$") {
      if (it.succeeded()) {
        val files = it.result()
          .stream()
          .map { name: String ->
            File(name)
          }
          .map(File::getName)
          .collect(Collectors.toList())
        val json: JsonObject = JsonObject().put("files", JsonArray(files))
        request.reply(json)
      } else {
        logger.error("readDir failed", it.cause())
        request.fail(500, it.cause().message)
      }
    }
  }

  private fun schedule(request: Message<JsonObject>) {
    val fileName = request.body().getString("file")
    if (playlist.isEmpty() && currentState == State.PAUSED) {
      currentState = State.PLAYING
    }
    playlist.offer(fileName)
  }

  private fun play(request: Message<Any>) {
    logger.trace("play: $request")
    currentState = State.PLAYING
  }

  private fun pause(request: Message<Any>) {
    logger.trace("pause: $request")
    currentState = State.PAUSED
  }

  private fun httpHandler(request: HttpServerRequest) {
    if("/" == request.path()) {
      openAudioStream(request)
      return
    }
    if(request.path().startsWith("/download/")) {
      val sanitizedPath = request.path().substring(10).replace("/", "")
      download(sanitizedPath, request)
      return
    }
    request.response().setStatusCode(404).end()
  }

  private fun streamAudioChunk(id: Long) {
    TODO("Implement")
  }
  private fun openAudioStream(request: HttpServerRequest) {
    val response = request.response()
    response
      .setChunked(true)
      .putHeader("Content-Type", "audio/mpeg")
    streamers.add(response)
    response.endHandler {
      streamers.remove(response)
      logger.info("A streamer left")
    }
  }
  private fun download(fileName:String, request: HttpServerRequest){
    TODO("implement")
  }
}
