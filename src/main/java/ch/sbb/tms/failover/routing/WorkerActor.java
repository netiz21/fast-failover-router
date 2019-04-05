package ch.sbb.tms.failover.routing;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.cluster.Cluster;

/**
 * Actual worker or "Routee":
 * Will be automatically added/removed to/from group of routees when he joins/leaves the cluster.
 */
public class WorkerActor extends AbstractLoggingActor {

    static Props props() {
        return Props.create(WorkerActor.class, WorkerActor::new);
    }

    private WorkerActor() {
    }

    @Override
    public void preStart() throws Exception {
        log().info("Worker starting..");
        super.preStart();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::pong)
                .matchAny(this::unmatched)
                .build();
    }

    private void unmatched(Object message) {
        log().warning("unmatched message: {} from {}", message, sender().path());
    }

    private void pong(String message) {
        log().info("Worker: {} received from {}", message, sender().path());
        sender().tell("pong from " + Cluster.get(context().system()).selfAddress(), self());
    }
}
