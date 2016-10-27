// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package config

import com.typesafe.config.ConfigFactory

trait ConfigurationStrings {
  final val config = ConfigFactory.load

  final val env = config.getString("cjww.environment")

  final val API_ID = config.getString(s"$env.application-ids.rest-api")
  final val AUTH_ID = config.getString(s"$env.application-ids.auth-service")
  final val DIAG_ID = config.getString(s"$env.application-ids.diagnostics-frontend")
  final val DEV_ID = config.getString(s"$env.application-ids.deversity-frontend")
}
