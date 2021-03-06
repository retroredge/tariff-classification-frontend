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

package uk.gov.hmrc.tariffclassificationfrontend.models

import java.time.{Clock, Instant}

import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.CaseStatus

case class Case
(
  reference: String,
  status: CaseStatus,
  createdDate: Instant,
  daysElapsed: Long,
  caseBoardsFileNumber: Option[String],
  assignee: Option[Operator],
  queueId: Option[String],
  application: Application,
  decision: Option[Decision],
  attachments: Seq[Attachment],
  keywords: Set[String] = Set.empty,
  sample: Sample = Sample()
) {
  def hasQueue: Boolean = queueId.isDefined

  def hasStatus(statuses: CaseStatus*): Boolean  = statuses.contains(status)

  def hasAssignee: Boolean = assignee.isDefined

  private def hasRuling: Boolean = {
    decision.flatMap(_.effectiveEndDate).isDefined
  }

  def hasExpiredRuling(implicit clock: Clock = Clock.systemUTC()): Boolean = {
    hasRuling && decision.flatMap(_.effectiveEndDate).exists(_.isBefore(Instant.now(clock)))
  }

  def hasLiveRuling(implicit clock: Clock = Clock.systemUTC()): Boolean = {
    hasRuling && !hasExpiredRuling
  }

  def isAssignedTo(operator: Operator): Boolean = {
    assignee.exists(_.id == operator.id)
  }

  def findAppeal(appealId: String): Option[Appeal] = {
    decision.flatMap(d => d.appeal.find(a => a.id.equals(appealId)))
  }

  def addAttachment(attachment: Attachment): Case = this.copy(attachments = this.attachments :+ attachment)

  def sampleToBeProvided: Boolean = {
    application.`type` match {
      case ApplicationType.BTI => application.asBTI.sampleToBeProvided
      case ApplicationType.LIABILITY_ORDER => sample.status.isDefined
    }
  }

  def sampleToBeReturned: Boolean = {
    application.`type` match {
      case ApplicationType.BTI => application.asBTI.sampleToBeReturned
      case ApplicationType.LIABILITY_ORDER => sample.returnStatus.contains(SampleReturn.YES)
    }
  }

}
