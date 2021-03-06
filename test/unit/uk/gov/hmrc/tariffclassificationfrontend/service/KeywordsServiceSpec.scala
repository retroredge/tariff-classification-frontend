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

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito.{reset, verify, verifyNoMoreInteractions, verifyZeroInteractions}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.audit.AuditService
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.{failed, successful}

class KeywordsServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val connector = mock[BindingTariffClassificationConnector]
  private val auditService = mock[AuditService]
  private val config = mock[AppConfig]

  private val service = new KeywordsService(connector, auditService)

  private val operator: Operator = Operator("operator-id", None)
  private val aCase = Cases.btiCaseExample

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, auditService, config)
  }

  "Add keyword" should {

    val aKeyword = "Apples"

    "propagate the error if the connector fails" in {
      given(connector.updateCase(any[Case])(any[HeaderCarrier]))
        .willReturn(failed(new IllegalStateException))

      intercept[IllegalStateException] {
        await(service.addKeyword(aCase, aKeyword, operator))
      }

      verifyZeroInteractions(auditService)
    }

    "add the keyword to the case keywords using the upper-case format" in {
      val aCaseWithNewKeyword = aCase.copy(keywords = aCase.keywords + "APPLES")

      given(connector.updateCase(refEq(aCaseWithNewKeyword))(any[HeaderCarrier]))
        .willReturn(successful(aCaseWithNewKeyword))

      await(service.addKeyword(aCase, aKeyword, operator)) shouldBe aCaseWithNewKeyword

      verify(auditService).auditCaseKeywordAdded(refEq(aCaseWithNewKeyword), refEq(aKeyword), refEq(operator))(refEq(hc))
      verifyNoMoreInteractions(auditService)
    }

    "ignore duplicates" in {
      val aCaseWithExistingKeyword = aCase.copy(keywords = aCase.keywords + "APPLES")

      await(service.addKeyword(aCaseWithExistingKeyword, aKeyword, operator)) shouldBe aCaseWithExistingKeyword

      verifyZeroInteractions(auditService)
    }
  }

  "Remove keyword" should {

    "propagate the error if the connector fails" in {
      val aKeyword = "Apples"

      val aCaseWithExistingKeyword = aCase.copy(keywords = aCase.keywords + "APPLES")

      given(connector.updateCase(any[Case])(any[HeaderCarrier]))
        .willReturn(failed(new IllegalStateException))

      intercept[IllegalStateException] {
        await(service.removeKeyword(aCaseWithExistingKeyword, aKeyword, operator))
      }

      verifyZeroInteractions(auditService)
    }

    "remove the keyword from the case keywords" in {
      val aKeyword = "Apples"
      val aCaseWithKeyword = aCase.copy(keywords = aCase.keywords + "APPLES")

      given(connector.updateCase(refEq(aCase))(any[HeaderCarrier])).willReturn(successful(aCase))

      await(service.removeKeyword(aCaseWithKeyword, aKeyword, operator)) shouldBe aCase

      verify(auditService).auditCaseKeywordRemoved(refEq(aCase), refEq(aKeyword), refEq(operator))(any[HeaderCarrier])
      verifyNoMoreInteractions(auditService)
    }

    "leave the case unchanged if the keyword is not part of the case keywords" in {
      val aKeyword = "Oranges"
      val aCaseWithKeyword = aCase.copy(keywords = aCase.keywords + "Apples")

      await(service.removeKeyword(aCaseWithKeyword, aKeyword, operator)) shouldBe aCaseWithKeyword

      verifyZeroInteractions(auditService)
    }
  }

  "Retrieve auto complete keywords" should {

    "return a list of keywords" in {
      await(service.autoCompleteKeywords) should contain ("ABS")
    }
  }

}
