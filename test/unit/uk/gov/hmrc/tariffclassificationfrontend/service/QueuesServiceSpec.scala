/*
 * Copyright 2018 HM Revenue & Customs
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

import org.mockito.BDDMockito.given
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Queue, Queues}

class QueuesServiceSpec extends UnitSpec with MockitoSugar {

  private val service = new QueuesService()

  "Get All Queues" should {

    "retrieve queues" in {
      service.getAll.size shouldBe 5
    }
  }

  "Get Non Gateway" should {

    "retrieve queues" in {
      service.getNonGateway.size shouldBe 4
    }
  }

  "Get Queue By Slug" should {

    "find relevant queue" in {
      service.getOneBySlug("gateway") shouldBe Some(Queue("1", "gateway", "Gateway"))
    }

    "not find unknown queue" in {
      service.getOneBySlug("unknown") shouldBe None
    }
  }

  "Get Queue By Id" should {

    "find relevant queue" in {
      service.getOneById("1") shouldBe Some(Queue("1", "gateway", "Gateway"))
    }

    "not find unknown queue" in {
      service.getOneById("0") shouldBe None
    }
  }

  "Find Queue Of" should {

    "find no queue for case without queue_id" in {
      val c = mock[Case]
      given(c.queueId).willReturn(None)
      service.findQueueOf(c) shouldBe None
    }

    "find no queue queue for case with unknown queue_id" in {
      val c = mock[Case]
      given(c.queueId).willReturn(Some("-1"))
      service.findQueueOf(c) shouldBe None
    }

    "find relevant queue for case" in {
      val c = mock[Case]
      given(c.queueId).willReturn(Some(Queues.gateway.id))
      service.findQueueOf(c) shouldBe Some(Queues.gateway)
    }
  }

}
