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

package uk.gov.hmrc.tariffclassificationfrontend.service

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.audit.AuditService
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.{BindingTariffClassificationConnector, RulingConnector}
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.successful

class CasesService_UpdateSampleReturnSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with ConnectorCaptor {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val connector = mock[BindingTariffClassificationConnector]
  private val rulingConnector = mock[RulingConnector]
  private val emailService = mock[EmailService]
  private val fileStoreService = mock[FileStoreService]
  private val reportingService = mock[ReportingService]
  private val audit = mock[AuditService]
  private val config = mock[AppConfig]
  private val aCase = Cases.btiCaseExample

  private val service = new CasesService(config, audit, emailService, fileStoreService, reportingService, connector, rulingConnector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, audit, config)
  }

  "Update Sample Return" should {

    "update case sample return" in {
      // Given
      val operator: Operator = Operator("operator-id", None)
      val originalCase = aCase.copy(sample = aCase.sample.copy(returnStatus = Some(SampleReturn.TO_BE_CONFIRMED)))
      val caseUpdated = aCase.copy(sample = aCase.sample.copy(returnStatus = Some(SampleReturn.YES)))

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])).willReturn(successful(mock[Event]))


      // When Then
      await(service.updateSampleReturn(originalCase, Some(SampleReturn.YES), operator)) shouldBe caseUpdated

      verify(audit).auditSampleReturnChange(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])


      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.sample.returnStatus shouldBe Some(SampleReturn.YES)

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id")
      eventCreated.details shouldBe SampleReturnChange(Some(SampleReturn.TO_BE_CONFIRMED), Some(SampleReturn.YES))
    }

  }

}
