package ch.sbb.tms.failover;

import akka.actor.ActorSystem;
import akka.management.AkkaManagement;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import ch.sbb.tms.failover.routing.Supervisor;

public class MainAppNode1 {

    public static void main(final String[] args) {
        // bootstrapping actor system + cluster
        ActorSystem system = ActorSystem.create("fast-failover");
        AkkaManagement.get(system).start();
        ClusterBootstrap.get(system).start();

        // start supervisor
        system.actorOf(Supervisor.props(), "routing");
    }
}
