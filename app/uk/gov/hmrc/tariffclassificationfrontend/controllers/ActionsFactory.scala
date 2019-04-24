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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, ActionRefiner, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AccessType._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class CheckPermissionsAction
  extends ActionRefiner[AuthenticatedCaseRequest, AuthenticatedCaseRequest] {

  override protected def refine[A](request: AuthenticatedCaseRequest[A]):
  Future[Either[Result, AuthenticatedCaseRequest[A]]] = {

    if (request.`case`.isAssignedTo(request.operator)) {
      authCaseRequest(request, READ_WRITE)
    } else {
      authCaseRequest(request, READ_ONLY)
    }
  }

  private def authCaseRequest[A](request: AuthenticatedCaseRequest[A], accessType: AccessType) = {
    successful(Right(new AuthenticatedCaseRequest(
      operator = request.operator,
      request = request,
      accessType = accessType,
      requestedCase = request.`case`)))
  }
}

@Singleton
class MustHaveWritePermissionAction
  extends ActionFilter[AuthenticatedCaseRequest] {

  override protected def filter[A](request: AuthenticatedCaseRequest[A]): Future[Option[Result]] = {
    val result =
      if (isAuthorized(request))
        None
      else
        Some(Redirect(routes.SecurityController.unauthorized()))

    successful(result)
  }

  private def isAuthorized[A](request: AuthenticatedCaseRequest[A]): Boolean = {
    request.`case`.isAssignedTo(request.operator) || request.operator.manager
  }
}

@Singleton
class VerifyCaseExistsActionFactory @Inject()(casesService: CasesService)(implicit val messagesApi: MessagesApi, appConfig: AppConfig) extends I18nSupport {

  def apply(reference: String): ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest] =
    new ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest] {
      override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, AuthenticatedCaseRequest[A]]] = {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
        implicit val r = request

        casesService.getOne(reference).flatMap {
          case Some(c: Case) =>
            successful(
              Right(
                new AuthenticatedCaseRequest(
                  operator = request.operator,
                  request = request,
                  requestedCase = c)
              )
            )
          case _ => successful(Left(NotFound(views.html.case_not_found(reference))))
        }
      }
    }
}