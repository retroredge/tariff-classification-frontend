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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials

import java.time.{ZoneOffset, ZonedDateTime}

import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes
import uk.gov.hmrc.tariffclassificationfrontend.forms.ActivityForm
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, _}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.activity_details
import uk.gov.tariffclassificationfrontend.utils.Cases._

class ActivityDetailsViewSpec extends ViewSpec {

  private val date = ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant
  private val queues = Seq(Queue(id = "1", slug = "test", name = "TEST"))

  "Activity Details" should {

    val requestWithMoveCasePermission = requestWithPermissions(Permission.MOVE_CASE_BACK_TO_QUEUE)
    val requestWithAddNotePermission = requestWithPermissions(Permission.ADD_NOTE)

    "Render empty events - showing 'Application Submitted'" in {
      // Given
      val c = aCase(
        withCreatedDate(date)
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-application_submitted")
      doc should containElementWithID("activity-events-row-application_submitted-operator")
      doc should containElementWithID("activity-events-row-application_submitted-content")
      doc should containElementWithID("activity-events-row-application_submitted-date")
      doc.getElementById("activity-events-row-application_submitted-date") should containText("01 Jan 2019")
    }

    "Render event without operator name" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = Note("comment"),
        operator = Operator("id", None),
        caseReference = "ref"
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("Unknown")
    }

    "Render 'Add Note' when user has permission" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = Note("comment"),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues)(requestWithAddNotePermission , messages, appConfig))

      // Then
      doc should containElementWithID("add-note-submit")
    }

    "Not Render 'Add Note' when user has no permissions" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = Note("comment"),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues)(operatorRequest , messages, appConfig))

      // Then
      doc shouldNot containElementWithID("add-note-submit")
    }

    "Render 'Note'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = Note("comment"),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Status Change'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = CaseStatusChange(from = CaseStatus.OPEN, to = CaseStatus.COMPLETED, comment = Some("comment"), attachmentId = Some("att-id")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Status changed from open to completed")
      doc should containElementWithID("activity-events-row-0-comment")
      doc.getElementById("activity-events-row-0-comment") should containText("comment")
      doc should containElementWithID("activity-events-row-0-email_link")
      doc.getElementById("activity-events-row-0-email_link") should haveAttribute("href", routes.ViewAttachmentController.get("att-id").url)
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Cancellation Status Change'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = CancellationCaseStatusChange(from = CaseStatus.OPEN, comment = Some("comment"), attachmentId = Some("att-id"), reason = CancelReason.INVALIDATED_CODE_CHANGE),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Status changed from open to cancelled")
      doc should containElementWithID("activity-events-row-0-comment")
      doc.getElementById("activity-events-row-0-comment") should containText("comment")
      doc should containElementWithID("activity-events-row-0-reason")
      doc.getElementById("activity-events-row-0-reason") should containText(CancelReason.format(CancelReason.INVALIDATED_CODE_CHANGE))
      doc should containElementWithID("activity-events-row-0-email_link")
      doc.getElementById("activity-events-row-0-email_link") should haveAttribute("href", routes.ViewAttachmentController.get("att-id").url)
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Referral Status Change'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = ReferralCaseStatusChange(from = CaseStatus.OPEN, comment = Some("comment"), attachmentId = Some("att-id"), referredTo = "Applicant", reason = Seq(ReferralReason.REQUEST_SAMPLE, ReferralReason.REQUEST_MORE_INFO)),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Status changed from open to referred")
      doc should containElementWithID("activity-events-row-0-comment")
      doc.getElementById("activity-events-row-0-comment") should containText("comment")
      doc should containElementWithID("activity-events-row-0-reason_0")
      doc.getElementById("activity-events-row-0-reason_0") should containText(ReferralReason.format(ReferralReason.REQUEST_SAMPLE))
      doc should containElementWithID("activity-events-row-0-reason_1")
      doc.getElementById("activity-events-row-0-reason_1") should containText(ReferralReason.format(ReferralReason.REQUEST_MORE_INFO))
      doc should containElementWithID("activity-events-row-0-email_link")
      doc.getElementById("activity-events-row-0-email_link") should haveAttribute("href", routes.ViewAttachmentController.get("att-id").url)
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Completed Status Change'" when {
      "Email is present" in {
        // Given
        val c = aCase()
        val e = Event(
          id = "EVENT_ID",
          details = CompletedCaseStatusChange(from = CaseStatus.OPEN, comment = Some("comment"), email = Some("some email")),
          operator = Operator("id", Some("name")),
          caseReference = "ref",
          timestamp = date
        )

        // When
        val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

        // Then
        doc should containElementWithID("activity-events-row-0-operator")
        doc.getElementById("activity-events-row-0-operator") should containText("name")
        doc should containElementWithID("activity-events-row-0-content")
        doc.getElementById("activity-events-row-0-content") should containText("Status changed from open to completed")
        doc should containElementWithID("activity-events-row-0-comment")
        doc.getElementById("activity-events-row-0-comment") should containText("comment")
        doc should containElementWithID("activity-events-row-0-email")
        doc.getElementById("activity-events-row-0-email") should containText("some email")
        doc should containElementWithID("activity-events-row-0-date")
        doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
      }

      "Email is absent" in {
        // Given
        val c = aCase()
        val e = Event(
          id = "EVENT_ID",
          details = CompletedCaseStatusChange(from = CaseStatus.OPEN, comment = Some("comment"), email = None),
          operator = Operator("id", Some("name")),
          caseReference = "ref",
          timestamp = date
        )

        // When
        val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

        // Then
        doc should containElementWithID("activity-events-row-0-operator")
        doc.getElementById("activity-events-row-0-operator") should containText("name")
        doc should containElementWithID("activity-events-row-0-content")
        doc.getElementById("activity-events-row-0-content") should containText("Status changed from open to completed")
        doc should containElementWithID("activity-events-row-0-comment")
        doc.getElementById("activity-events-row-0-comment") should containText("comment")
        doc shouldNot containElementWithID("activity-events-row-0-email")
        doc should containElementWithID("activity-events-row-0-date")
        doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
      }
    }

    "Render 'Appeal Added'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AppealAdded(appealType = AppealType.APPEAL_TIER_1, appealStatus = AppealStatus.IN_PROGRESS, comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Appeal added with type Appeal tier 1 and status Under appeal")
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Review Added'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AppealAdded(appealType = AppealType.REVIEW, appealStatus = AppealStatus.IN_PROGRESS, comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Review added with status Under review")
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Appeal Status Changed'" in {
      // Given
      val c = aCase(withDecision(appeal = Seq(Appeal("id", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1))), withStatus(CaseStatus.COMPLETED))
      val e = Event(
        id = "EVENT_ID",
        details = AppealStatusChange(appealType = AppealType.APPEAL_TIER_1, from = AppealStatus.IN_PROGRESS, to = AppealStatus.ALLOWED, comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Appeal tier 1 status changed from Under appeal to Appeal allowed")
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Extended Use Change'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = ExtendedUseStatusChange(from = false, to = true, comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Application for extended use status changed from No to Yes")
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Assignment Change'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AssignmentChange(from = None, to = None, comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Assignment Change' from Some to Some" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AssignmentChange(from = Some(Operator("from", Some("FROM"))), to = Some(Operator("to", Some("TO"))), comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case reassigned from FROM to TO")
    }

    "Render 'Assignment Change' from Some to None" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AssignmentChange(from = Some(Operator("from", Some("FROM"))), to = None, comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case unassigned from FROM")
    }

    "Render 'Assignment Change' from None to Some" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AssignmentChange(from = None, to = Some(Operator("to", Some("TO"))), comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case assigned to TO")
    }

    "Render 'Assignment Change' from None to None" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AssignmentChange(from = None, to = None, comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case assignment changed")
    }

    "Not render assigned information if does not have right permissions" in {
      // Given
      val c = aCase(
        withAssignee(Some(authenticatedOperator))
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues))

      // Then
      doc shouldNot containElementWithID("activity-events-assignee")
      doc shouldNot containElementWithID("activity-events-assignee-label")
    }

    "Render assigned to 'You'" in {
      // Given
      val c = aCase(
        withAssignee(Some(authenticatedOperator))
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues)(request = requestWithPermissions(Permission.VIEW_CASE_ASSIGNEE), messages, appConfig))

      // Then
      doc should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee").text() shouldBe "You"
      doc.getElementById("activity-events-assignee-label").text() shouldBe "Currently assigned to:"
    }

    "Render assigned to name" in {
      // Given
      val c = aCase(
        withAssignee(Some(Operator("id", Some("name"))))
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues)(request = requestWithPermissions(Permission.VIEW_CASE_ASSIGNEE), messages, appConfig))

      // Then
      doc should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee").text() shouldBe "name"
      doc.getElementById("activity-events-assignee-label").text() shouldBe "Currently assigned to:"
    }

    "Render assigned to PID" in {
      // Given
      val c = aCase(
        withAssignee(Some(Operator("id")))
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues)(request = requestWithPermissions(Permission.VIEW_CASE_ASSIGNEE), messages, appConfig))

      // Then
      doc should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee").text() shouldBe "PID id"
      doc.getElementById("activity-events-assignee-label").text() shouldBe "Currently assigned to:"
    }

    "Render currently in 'TEST'" in {
      // Given
      val c = aCase(
        withAssignee(None),
        withQueue("1")
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues)(request = requestWithPermissions(Permission.VIEW_CASE_ASSIGNEE), messages, appConfig))

      // Then
      doc should containElementWithID("activity-events-assigned-queue")
      doc.getElementById("activity-events-assigned-queue").text() shouldBe "TEST"
      doc.getElementById("activity-events-assigned-queue-label").text() shouldBe "Currently in:"
    }

    "Render currently in 'Gateway'" in {
      // Given
      val c = aCase(
        withAssignee(None)
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues)(request = requestWithPermissions(Permission.VIEW_CASE_ASSIGNEE), messages, appConfig))

      // Then
      doc should containElementWithID("activity-events-assigned-queue")
      doc.getElementById("activity-events-assigned-queue").text() shouldBe "Gateway"
      doc.getElementById("activity-events-assigned-queue-label").text() shouldBe "Currently in:"
    }

    "Render currently in 'unknown'" in {
      // Given
      val c = aCase(
        withAssignee(None),
        withQueue("99")
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues)(request = requestWithPermissions(Permission.VIEW_CASE_ASSIGNEE), messages, appConfig))

      // Then
      doc should containElementWithID("activity-events-assigned-queue")
      doc.getElementById("activity-events-assigned-queue").text() shouldBe "unknown"
      doc.getElementById("activity-events-assigned-queue-label").text() shouldBe "Currently in:"
    }

    "Render 'Queue Change' from Some to Some" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = QueueChange(from = Some("2"), to = Some("1"), comment = Some("comment")),
        operator = Operator("id", Some("Name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Name moved this case to the TEST queue")
    }

    "Not Render 'Reassign Link' When Case is not assigned" in {

      // Given
      val c = aCase(
        withAssignee(None),
        withStatus(CaseStatus.OPEN)
      )
      // When
      val doc = view(activity_details(c, Paged(Seq.empty), ActivityForm.form, queues))

      // Then
      doc shouldNot containElementWithID("reassign-queue-link")
    }

    "Not render 'Reassign Link' when valid state but no permissions " in {

      Set(CaseStatus.OPEN, CaseStatus.REFERRED, CaseStatus.SUSPENDED).foreach(status => {
        // Given
        val c = aCase(
          withAssignee(Some(Operator("id"))),
          withStatus(status)
        )

        // When
        val doc = view(activity_details(c, Paged(Seq.empty), ActivityForm.form, queues)(operatorRequest, messages, appConfig))

        // Then
        doc shouldNot containElementWithID("reassign-queue-link")
      })
    }

    "Render 'Reassign Link' when valid state and permissions are MOVE_CASE_BACK_TO_QUEUE " in {

      Set(CaseStatus.OPEN, CaseStatus.REFERRED, CaseStatus.SUSPENDED).foreach(status => {
        // Given
        val c = aCase(
          withAssignee(Some(Operator("id"))),
          withStatus(status)
        )

        // When
        val doc = view(activity_details(c, Paged(Seq.empty), ActivityForm.form, queues)(requestWithMoveCasePermission, messages, appConfig))

        // Then
        doc should containElementWithID("reassign-queue-link")
      })

    }
  }


}
