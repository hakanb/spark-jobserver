package spark.jobserver.common.akka

import org.scalatest.{BeforeAndAfterAll, Matchers, FunSpec}
import akka.testkit.{TestKit, TestActorRef}

import akka.actor.{Actor, ActorSystem}


class DummyActor extends ActorStack {
  var str = ""
  def wrappedReceive: Receive = {
    case s: String => str += s
  }

  override def unhandled(x: Any) { str = "unhandled" }
}

trait AddPrefix extends ActorStack {
  override def receive: Receive = {
    case x: String =>
      super.receive("pre " + x.asInstanceOf[String])
  }
}

class ActorStackSpec extends FunSpec with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem("test")

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  describe("stacking traits") {
    it("should be able to stack traits and receive messages") {
      val actorRef = TestActorRef(new DummyActor with AddPrefix)
      val actor = actorRef.underlyingActor

      actorRef ! "me"
      actor.str should equal ("pre me")
    }

    it("should pass messages not handled in wrappedReceive to unhandled function") {
      val actorRef = TestActorRef[DummyActor]
      val actor = actorRef.underlyingActor

      actorRef ! List(1, 2)
      actor.str should equal ("unhandled")
    }
  }
}
          