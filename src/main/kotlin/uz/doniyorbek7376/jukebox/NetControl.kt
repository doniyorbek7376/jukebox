package uz.doniyorbek7376.jukebox

import io.vertx.core.AbstractVerticle
import io.vertx.core.buffer.Buffer
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetSocket
import io.vertx.core.parsetools.RecordParser

class NetControl : AbstractVerticle() {
  private val logger = LoggerFactory.getLogger(NetControl::class.java)
  override fun start() {
    vertx.createNetServer()
      .connectHandler(this::handleClient)
      .listen(3000)
  }

  private fun handleClient(socket: NetSocket) {
    RecordParser.newDelimited("/n", socket)
      .handler {
        handleBuffer(socket, it)
      }
      .endHandler {
        logger.info("connection ended")
      }
  }

  private fun handleBuffer(socket: NetSocket, buffer: Buffer) {
    when (val command = buffer.toString()) {
      "/list" -> listCommand(socket)
      "/play" -> vertx.eventBus().send("jukebox.play", "")
      "/pause" -> vertx.eventBus().send("jukebox.pause", "")
      else -> {
        if (command.startsWith("/schedule")) {
          schedule(command)
        } else {
          socket.write("Unknown command")
        }
      }
    }
  }

  private fun listCommand(socket: NetSocket) {
    vertx.eventBus().request<JsonObject>("jukebox.list", "") {
      if(it.succeeded()) {
        val files = it.result().body().getJsonArray("files")
        files.stream().forEach { file ->
          socket.write("$file\n")
        }
      }
      else {
        logger.error("Failed to get list of tracks", it.cause())
      }
    }
  }

  private fun schedule(command: String) {
    val track = command.substring(10)
    vertx.eventBus().send("jukebox.schedule", JsonObject().put("file", track))
  }

}
