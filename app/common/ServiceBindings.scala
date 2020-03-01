/*
 * Copyright 2020 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package common

import com.cjwwdev.config.{ConfigurationLoader, DefaultConfigurationLoader}
import com.cjwwdev.featuremanagement.models.Features
import com.cjwwdev.health.{DefaultHealthController, HealthController}
import com.cjwwdev.logging.filters.{DefaultRequestLoggingFilter, RequestLoggingFilter}
import com.cjwwdev.mongo.indexes.RepositoryIndexer
import com.cjwwdev.scheduling.ScheduledJob
import controllers.{DefaultSessionController, SessionController}
import jobs.SessionCleanJob
import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}
import repositories.{DefaultSessionRepository, SessionRepository}
import services.{DefaultSessionService, SessionService}

class ServiceBindings extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =Seq(
    bind(classOf[ConfigurationLoader]).to(classOf[DefaultConfigurationLoader]).eagerly(),
    bind(classOf[SessionRepository]).to(classOf[DefaultSessionRepository]).eagerly(),
    bind(classOf[RepositoryIndexer]).to(classOf[SessionStoreIndexing]).eagerly(),
    bind(classOf[SessionService]).to(classOf[DefaultSessionService]).eagerly(),
    bind(classOf[SessionController]).to(classOf[DefaultSessionController]).eagerly(),
    bind(classOf[HealthController]).to(classOf[DefaultHealthController]).eagerly(),
    bind(classOf[ScheduledJob]).to(classOf[SessionCleanJob]).eagerly(),
    bind(classOf[Features]).to(classOf[FeatureDef]).eagerly(),
    bind(classOf[RequestLoggingFilter]).to(classOf[DefaultRequestLoggingFilter]).eagerly()
  )
}
