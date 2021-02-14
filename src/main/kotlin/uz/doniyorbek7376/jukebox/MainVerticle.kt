package uz.doniyorbek7376.jukebox

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.Vertx

class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    vertx.deployVerticle(JukeBox())
    vertx.deployVerticle(NetControl())
  }
}
fun main() {
  Vertx.vertx().deployVerticle(MainVerticle())
}
