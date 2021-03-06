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

package uk.gov.hmrc.tariffclassificationfrontend.forms

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.tariffclassificationfrontend.forms.mappings.FormMappings.textNonEmpty
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseReferral

object ReferCaseForm {


  lazy val form: Form[CaseReferral] = Form(mapping(
    "referredTo" -> textNonEmpty("error.empty.refer.to"),
    "reasons" -> list(text),
    "note" -> textNonEmpty("error.empty.refer.note"),
    "referManually" -> optional(text)
  )(CaseReferral.apply)(CaseReferral.unapply)
  )
}
