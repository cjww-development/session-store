/*
 *  Copyright 2018 CJWW Development
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package jobs

import javax.inject.Inject
import akka.actor.ActorSystem
import com.cjwwdev.config.ConfigurationLoader

import com.cjwwdev.scheduling._
import services.SessionService

import scala.concurrent.{ExecutionContext, Future}

class SessionCleanJob @Inject()(val actorSystem: ActorSystem,
                                val config: ConfigurationLoader,
                                val sessionService: SessionService,
                                implicit val executionContext: ExecutionContext) extends ScheduledJob {
  lazy val jobName  = "session-cleaner"
  lazy val enabled  = config.get[Boolean](s"jobs.$jobName.enabled")
  lazy val interval = config.get[Long](s"jobs.$jobName.interval")

  def scheduledJob: Future[JobCompletionStatus] = {
    sessionService.cleanseSessions map {
      _ => JobComplete
    } recover {
      case _ => JobFailed
    }
  }

  if(enabled) jobRunner(scheduledJob) else Future(JobDisabled)
}
