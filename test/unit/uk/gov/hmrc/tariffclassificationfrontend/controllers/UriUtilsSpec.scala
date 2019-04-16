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

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.play.test.UnitSpec

class UriUtilsSpec extends WordSpec with Matchers with UnitSpec {

  "URI Utils" should {

    "return case number when expected uri with case reference in the middle" in {
      val uri = "digital-tariff/cases/12345678/whatever"
      UriUtils.extractCaseReference(uri) shouldBe "12345678"
    }

    "return case number when expected uri with case reference in the end" in {
      val uri = "digital-tariff/cases/12345678"
      UriUtils.extractCaseReference(uri) shouldBe "12345678"
    }

    "return case number when expected uri starts with slash" in {
      val uri = "/tariff-classification/cases/1/suspend"
      UriUtils.extractCaseReference(uri) shouldBe "1"
    }

    "return case number when expected uri starts with cases keyword" in {
      val uri = "cases/1/suspend"
      UriUtils.extractCaseReference(uri) shouldBe "1"
    }

  }

}
