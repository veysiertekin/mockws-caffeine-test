import com.github.benmanes.caffeine.cache.Ticker

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.duration.Duration

class FakeTicker extends Ticker {
  private val nanos                  = new AtomicLong()
  private val autoIncrementStepNanos = new AtomicLong()

  override def read(): Long =
    nanos.getAndAdd(autoIncrementStepNanos.get())

  def advance(duration: Duration): FakeTicker = {
    advance(duration.toNanos)
    this
  }

  def advance(nanoseconds: Long): FakeTicker = {
    nanos.addAndGet(nanoseconds)
    this
  }

  def setAutoIncrement(duration: Duration): Unit = {
    this.autoIncrementStepNanos.set(duration.toNanos)
  }
}
