package com.sos.scheduler.engine.plugins.newwebservice.routes

import com.sos.scheduler.engine.common.scalautil.FileUtils.implicits._
import com.sos.scheduler.engine.common.sprayutils.SprayUtils.completeWithError
import com.sos.scheduler.engine.common.sprayutils.SprayUtils.passSome
import com.sos.scheduler.engine.common.sprayutils.SprayUtils.passIf
import com.sos.scheduler.engine.kernel.scheduler.SchedulerConfiguration
import com.sos.scheduler.engine.plugins.newwebservice.routes.cpp.CppHttpRoute
import java.nio.file.Path
import spray.http.StatusCodes._
import spray.http.Uri
import spray.routing.Directives._
import spray.routing._

/**
  * @author Joacim Zschimmer
  */
trait JocCompatibleRoute extends CommandRoute with CppHttpRoute {

  protected def schedulerConfiguration: SchedulerConfiguration

  final def jocCompatibleRoute: Route =
    (pathPrefix("joc") | pathPrefix("operations_gui")) {
      passSome(schedulerConfiguration.htmlDirOption)(jocRoute)
    } ~
    pathPrefix("engine") {
      (pathPrefix("command") & pathEndOrSingleSlash) {  // SingleSlash for compatibility with JOC 1
        untypedPostCommandRoute
      }
    } ~
    pathPrefix("engine-cpp") {
      cppHttpRoute
    }

  private def jocRoute(directory: Path): Route =
    get {
      pathEnd {
        requestInstance { request ⇒
          passIf(request.uri.query == Uri.Query.Empty) {
            val withSlash = request.uri.copy(
              scheme = "",
              authority = Uri.Authority.Empty,
              path = Uri.Path(request.uri.path.toString + "/"))
            redirect(withSlash, TemporaryRedirect)
          }
        }
      } ~
      pathSingleSlash {
        getFromFile(directory / "index.html")
      } ~
      path("index.html") {
        requestInstance { request ⇒
          val withoutFile = request.uri.copy(
            scheme = "",
            authority = Uri.Authority.Empty,
            path = request.uri.path.reverse.tail.reverse)
          redirect(withoutFile, TemporaryRedirect)
        }
        //complete((NotFound, "Not Found"))
      } ~ {
        implicit val resolver = OurContentTypeResolver
        getFromDirectory(directory.toString)
      }
    }
}