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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, FileStoreService, PdfService}
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.successful

class PdfDownloadControllerSpec extends UnitSpec with MockitoSugar with ControllerCommons with WithFakeApplication {

  private val fakeRequest = FakeRequest()
  private val pdfService = mock[PdfService]
  private val caseService = mock[CasesService]
  private val fileService = mock[FileStoreService]
  private val operator = mock[Operator]

  private val decision = Decision(bindingCommodityCode = "040900", justification = "justification-content",
    goodsDescription = "goods-description", methodSearch = Some("method-to-search"))

  private val expectedResult = PdfFile("Some content".getBytes)

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))

  private implicit val appConfig: AppConfig = new AppConfig(configuration, env)
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val caseWithDecision = Cases.btiCaseExample.copy(decision = Some(decision))
  private val caseWithoutDecision = Cases.btiCaseExample.copy(decision = None)
  private val liabilityCaseWithDecision = Cases.liabilityCaseExample.copy(decision = Some(decision))

  private val controller = new PdfDownloadController(new SuccessfulAuthenticatedAction(operator), messageApi, pdfService, fileService, caseService)

  private def givenCompletedCase(): Unit = {
    when(caseService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(Some(caseWithDecision)))
  }

  private def givenCompletedLiabilityCase(): Unit = {
    when(caseService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(Some(liabilityCaseWithDecision)))
  }

  private def givenNonDecisionCase(): Unit = {
    when(caseService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(Some(caseWithoutDecision)))
  }

  private def givenCaseWithoutAttachments(): Unit = {
    when(fileService.getAttachments(any[Case])(any[HeaderCarrier])).thenReturn(successful(Seq.empty))
  }

  private def givenCaseWithoutLetterOfAuth(): Unit = {
    when(fileService.getLetterOfAuthority(any[Case])(any[HeaderCarrier])).thenReturn(successful(None))
  }

  private def givenValidGeneratedPdf(): Unit = {
    when(pdfService.generatePdf(any[Html])).thenReturn(successful(expectedResult))
  }

  private def givenNotFoundCase(): Unit = {
    when(caseService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(None))
  }


  "PdfDownloadController Application" must {

    "return expected pdf" in {
      givenCompletedCase()
      givenCaseWithoutAttachments()
      givenCaseWithoutLetterOfAuth()
      givenValidGeneratedPdf()

      val result: Result = await(controller.applicationPdf(caseWithDecision.reference)(fakeRequest))

      status(result) shouldBe OK
      contentAsString(result) shouldBe "Some content"
      contentType(result) shouldBe Some("application/pdf")
    }

    "error when case not found" in {
      givenNotFoundCase()

      val result = await(controller.applicationPdf(caseWithDecision.reference)(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

  }

  "PdfDownloadController Ruling" must {

    "return expected pdf" in {
      givenCompletedCase()
      givenValidGeneratedPdf()

      val result = await(controller.getRulingPdf(caseWithDecision.reference)(fakeRequest))

      status(result) shouldBe OK
      contentAsString(result) shouldBe "Some content"
      contentType(result) shouldBe Some("application/pdf")
      header("Content-Disposition",result) shouldBe Some("filename=BTIRuling_1.pdf")
    }

    "return expected pdf for liability case" in {
      givenCompletedLiabilityCase()
      givenValidGeneratedPdf()

      val result = await(controller.getRulingPdf(liabilityCaseWithDecision.reference)(fakeRequest))

      status(result) shouldBe OK
      contentAsString(result) shouldBe "Some content"
      contentType(result) shouldBe Some("application/pdf")
      header("Content-Disposition",result) shouldBe Some("filename=LiabilityDecision_1.pdf")
    }

    "redirect to ruling when no decision found" in {
      givenNonDecisionCase()
      givenValidGeneratedPdf()

      val result = await(controller.getRulingPdf(caseWithDecision.reference)(fakeRequest))

      status(result) shouldBe Status.OK
      locationOf(result) shouldBe None
      contentAsString(result) should include("We could not find a ruling with case reference")
    }

    "error when case not found" in {
      givenNotFoundCase()

      val result = await(controller.getRulingPdf(caseWithDecision.reference)(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

  }
}
