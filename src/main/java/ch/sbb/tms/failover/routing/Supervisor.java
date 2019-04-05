package ch.sbb.tms.failover.routing;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;

import java.time.Duration;

/** Supervisor or root actor of our actor system */
public class Supervisor extends AbstractLoggingActor {

    private ActorRef router;
    private Cancellable schedule;

    public static Props props() {
        return Props.create(Supervisor.class, Supervisor::new);
    }

    private Supervisor() {
    }

    @Override
    public void preStart() throws Exception {
        log().info("Supervisor starting..");

        // start worker actor (aka. "routee")
        context().actorOf(WorkerActor.props(), "worker");

        // start routing actor (holds the cluster aware router)
        router = context().actorOf(RouterActor.props(), "router");

        // continuously send a "ping" via routing actor to each of the workers (routee) in the cluster
        // -> first response is accepted, all other will be discarded (group: scatter-gather-first-completed)
        schedule = schedulePings();

        super.preStart();
    }

    @Override
    public void postStop() throws Exception {
        if (schedule != null && !schedule.isCancelled()) {
            schedule.cancel();
        }
        super.postStop();
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::responseHandling)
                .build();
    }

    private void responseHandling(String message) {
        log().info("Response received: {} ({})", message, sender().path().address().toString());
    }

    private Cancellable schedulePings() {
        return context().system().scheduler().schedule(
                Duration.ofSeconds(3),
                Duration.ofSeconds(2),
                router,
                "ping",
                context().dispatcher(),
                self());

    }

}