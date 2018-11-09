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

import sbt._
import play.sbt.PlayImport._

object AppDependencies {
  def apply(): Seq[ModuleID] = CompileDependencies()  ++ UnitTestDependencies() ++ IntegrationTestDependencies()
}

private object CompileDependencies {
  private val reactiveMongoVersion     = "7.2.2"
  private val authVersion              = "4.6.0"
  private val appUtilsVersion          = "4.5.2"
  private val serviceHealthVersion     = "0.3.1"
  private val featureManagementVersion = "1.4.0"

  private val includedDeps: Seq[ModuleID] = Seq(
    guice
  )

  private val appDependencies: Seq[ModuleID] = Seq(
    "com.cjww-dev.libs" % "reactive-mongo_2.12"        % reactiveMongoVersion,
    "com.cjww-dev.libs" % "authorisation_2.12"         % authVersion,
    "com.cjww-dev.libs" % "application-utilities_2.12" % appUtilsVersion,
    "com.cjww-dev.libs" % "service-health_2.12"        % serviceHealthVersion,
    "com.cjww-dev.libs" % "feature-management_2.12"    % featureManagementVersion
  )

  def apply(): Seq[ModuleID] = appDependencies
}

private trait CommonTestDependencies {
  protected val testFrameworkVersion = "3.2.0"

  val scope: Configuration
  val testDependencies: Seq[ModuleID]
}

private object UnitTestDependencies extends CommonTestDependencies {
  override val scope: Configuration = Test
  
  override val testDependencies: Seq[ModuleID] = Seq(
    "com.cjww-dev.libs" % "testing-framework_2.12" % testFrameworkVersion % scope
  )

  def apply(): Seq[ModuleID] = testDependencies
}

private object IntegrationTestDependencies extends CommonTestDependencies {
  override val scope: Configuration = IntegrationTest

  override val testDependencies: Seq[ModuleID] = Seq(
    "com.cjww-dev.libs" % "testing-framework_2.12" % testFrameworkVersion % scope
  )

  def apply(): Seq[ModuleID] = testDependencies
}
