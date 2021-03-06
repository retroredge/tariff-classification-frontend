/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.connector

import akka.actor.ActorSystem
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.tariffclassificationfrontend.utils.{ResourceFiles, WiremockTestServer}

trait ConnectorTest extends UnitSpec with WithFakeApplication
  with WiremockTestServer with MockitoSugar with ResourceFiles
  with BeforeAndAfterAll {

  private val actorSystem = ActorSystem.create("testActorSystem")

  protected implicit val realConfig: AppConfig = fakeApplication.injector.instanceOf[AppConfig]
  protected val appConfig: AppConfig = mock[AppConfig]

  protected val fakeAuthToken = "AUTH_TOKEN"

  protected implicit val hc: HeaderCarrier = HeaderCarrier()

  private val environment = fakeApplication.injector.instanceOf[Environment]
  private val auditConnector = new DefaultAuditConnector(fakeApplication.configuration, environment)

  protected val wsClient: WSClient = fakeApplication.injector.instanceOf[WSClient]

  protected val authenticatedHttpClient = new AuthenticatedHttpClient(auditConnector, wsClient, actorSystem)
  protected val standardHttpClient = new DefaultHttpClient(fakeApplication.configuration, auditConnector, wsClient, actorSystem)

  override def beforeAll(): Unit = {
    super.beforeAll()

    when(appConfig.fileStoreUrl) thenReturn getUrl
    when(appConfig.bindingTariffClassificationUrl) thenReturn getUrl
    when(appConfig.rulingUrl) thenReturn getUrl

    when(appConfig.emailUrl) thenReturn getUrl
    when(appConfig.emailRendererUrl) thenReturn getUrl
    when(appConfig.pdfGeneratorUrl) thenReturn getUrl

    when(appConfig.apiToken) thenReturn fakeAuthToken
  }

}
