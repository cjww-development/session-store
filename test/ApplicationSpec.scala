import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends PlaySpec with OneAppPerTest {

  "Routes" should {

    "send 404 on a bad request" in  {
      assert(1 == 1)
    }

  }

  "HomeController" should {

    "render the index page" in {
      assert(1 == 1)
    }

  }

  "CountController" should {

    "return an increasing count" in {
      assert(1 == 1)
    }

  }

}
