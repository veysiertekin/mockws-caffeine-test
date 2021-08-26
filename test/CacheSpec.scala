import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import mockws.{MockWS, Route}
import org.awaitility.Awaitility.await
import org.scalatest.freespec.AnyFreeSpec
import play.api.mvc.Results.Ok
import play.api.test.Helpers

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.Random

class CacheSpec extends AnyFreeSpec {
  import mockws.MockWSHelpers._

  implicit val ec: ExecutionContextExecutor = materializer.executionContext

  val fakeTicker = new FakeTicker

  case class Expired(ttl: Long, data: String)

  val cache: AsyncLoadingCache[String, Expired] = Scaffeine()
    .executor(ec)
    .ticker(fakeTicker)
    .refreshAfterWrite(1.hour)
    .expireAfter(
      create = (_: String, response: Expired) => response.ttl.milliseconds,
      update = (_: String, response: Expired, _: FiniteDuration) => response.ttl.milliseconds,
      read = (_: String, _: Expired, duration: FiniteDuration) => duration
    )
    .buildAsyncFuture[String, Expired](load(_))

  val ws: MockWS = MockWS {
    Route {
      case ("POST", "/test") =>
        Action {
          Ok(Random.nextString(10))
        }
    }
  }

  def load(s: String): Future[Expired] = {
    //Does not work:
    ws.url("/test").post("test").map { result => Expired(600000, result.body) }
    //Works:
    //Future(Expired(600000, Random.nextString(10)))
  }

  "get" - {
    "should pass" in {
      def isNotSame(cache: AsyncLoadingCache[String, Expired], data: Expired): Boolean =
        Helpers.await(cache.get("test"), 1, TimeUnit.MINUTES) != data
      val first = Helpers.await(cache.get("test"), 1, TimeUnit.MINUTES)
      println(first)
      fakeTicker.advance(4.hours)
      await()
        .atMost(1, TimeUnit.MINUTES)
        .until(() => isNotSame(cache, first))
      val second = Helpers.await(cache.get("test"), 1, TimeUnit.MINUTES)
      println(second)
      assert(first != second)
    }
  }
}
