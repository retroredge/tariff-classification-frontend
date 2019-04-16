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

import javax.inject.{Inject, Singleton}
import play.api.mvc.ActionBuilder
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedCaseRequest

@Singleton
class RequestActions @Inject()(authoriseCase: AuthoriseCaseAction,
                               readOnlyCase: ReadOnlyCaseAction,
                               caseExistAction: CaseExistAction,
                               _authenticated: AuthenticatedAction){

  def authorisedWithWriteAccess: ActionBuilder[AuthenticatedCaseRequest] = _authenticated andThen authoriseCase
  def authorised: ActionBuilder[AuthenticatedCaseRequest] = _authenticated andThen readOnlyCase
  def caseExists(reference: String): CaseExistAction = caseExistAction(reference)
  def authenticated(reference: String): CaseExistAction = caseExistAction(reference)
  _authenticated
}
