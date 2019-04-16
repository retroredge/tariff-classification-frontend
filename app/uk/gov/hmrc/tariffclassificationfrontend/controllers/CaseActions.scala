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
import play.api.mvc.Results._
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.tariffclassificationfrontend.controllers.UriUtils.extractCaseReference
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AccessType.AccessType
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AccessType, AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Operator}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class ReadOnlyCaseAction @Inject()(override val casesService: CasesService)
  extends ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest]
    with CommonCaseAction {

  override protected def refine[A](request: AuthenticatedRequest[A]):
                Future[Either[Result, AuthenticatedCaseRequest[A]]] = {
    checkCasePermissions(request, c => Right(authCaseRequest(request, c, AccessType.READ_ONLY)))
  }
}

@Singleton
class AuthoriseCaseAction @Inject()(override val casesService: CasesService)
  extends ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest]  with CommonCaseAction  {

  override protected def refine[A](request: AuthenticatedRequest[A]):
  Future[Either[Result, AuthenticatedCaseRequest[A]]] = {
    checkCasePermissions(request, _ => Left(Redirect(routes.SecurityController.unauthorized())))
  }
}

@Singleton
class CaseExistAction @Inject()(reference : String)(implicit casesService: CasesService)
  extends ActionRefiner[AuthenticatedRequest, AuthenticatedCaseRequest] {

  override protected def refine[A](request: AuthenticatedRequest[A]):
  Future[Either[Result, AuthenticatedCaseRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(
      request.headers,
      Some(request.session)
    )

    casesService.getOne(reference).flatMap {
      case Some(c: Case) => successful(Right(authCaseRequest(request, c)))
      case _ => successful(Left(Redirect(routes.CaseController.caseNotFound(reference))))
    }

    def authCaseRequest[A](request: AuthenticatedRequest[A], c: Case): AuthenticatedCaseRequest[A] = {
      new AuthenticatedCaseRequest(
        operator = request.operator,
        request = request,
        _c = Some(c))
    }
  }
}

trait CommonCaseAction {

  implicit val casesService : CasesService

  def checkCasePermissions[A](request: AuthenticatedRequest[A], onDeniedPermission: Case => Either[Result, AuthenticatedCaseRequest[A]])
                      : Future[Either[Result,AuthenticatedCaseRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(
      request.headers,
      Some(request.session)
    )

    val reference = extractCaseReference(request.request.uri).get

    casesService.getOne(reference).flatMap {
      case Some(c: Case) if c.isAssignedTo(request.operator) => successful(Right(authCaseRequest(request, c, AccessType.READ_WRITE)))
      case Some(c: Case) => successful(onDeniedPermission(c))
      case _ => successful(Left(Redirect(routes.CaseController.caseNotFound(reference))))
    }
  }

  def authCaseRequest[A](request: AuthenticatedRequest[A], c: Case, accessType: AccessType): AuthenticatedCaseRequest[A] = {
    new AuthenticatedCaseRequest(
      operator = request.operator,
      request = request,
      accessType = accessType,
      _c = Some(c))
  }
}

object UriUtils {
  //TODO
  def extractCaseReference(uri : String): Option[String] = {
    uri.split("/").lift(3)
  }

}


