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
 */

package config

import com.cjwwdev.config.{ConfigurationLoader, ConfigurationLoaderImpl}
import com.google.inject.AbstractModule
import controllers.{SessionController, SessionControllerImpl}
import repositories.{SessionRepository, SessionRepositoryImpl}
import services.{SessionService, SessionServiceImpl}

class ServiceBindings extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ConfigurationLoader]).to(classOf[ConfigurationLoaderImpl]).asEagerSingleton()
    bind(classOf[SessionRepository]).to(classOf[SessionRepositoryImpl]).asEagerSingleton()
    bind(classOf[SessionService]).to(classOf[SessionServiceImpl]).asEagerSingleton()
    bind(classOf[SessionController]).to(classOf[SessionControllerImpl]).asEagerSingleton()
  }
}