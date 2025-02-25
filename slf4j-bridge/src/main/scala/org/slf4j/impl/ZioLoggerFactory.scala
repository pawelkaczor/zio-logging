package org.slf4j.impl

import com.github.ghik.silencer.silent
import org.slf4j.{ ILoggerFactory, Logger }
import zio.{ Unsafe, ZIO }

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.ConcurrentMapHasAsScala

class ZioLoggerFactory extends ILoggerFactory {
  private var runtime: zio.Runtime[Any] = _
  private val loggers                   = new ConcurrentHashMap[String, Logger]().asScala: @silent("JavaConverters")

  def attachRuntime(runtime: zio.Runtime[Any]): Unit =
    this.runtime = runtime

  private[impl] def run(f: ZIO[Any, Nothing, Any]): Unit =
    if (runtime != null) {
      Unsafe.unsafeCompat(implicit u => runtime.unsafe.run(f))
      ()
    }

  override def getLogger(name: String): Logger =
    loggers.getOrElseUpdate(name, new ZioLogger(name, this))
}

object ZioLoggerFactory {
  def initialize(runtime: zio.Runtime[Any]): Unit =
    StaticLoggerBinder.getSingleton.getLoggerFactory
      .asInstanceOf[ZioLoggerFactory]
      .attachRuntime(runtime)
}
