package ch.sbb.tms.failover.routing;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.routing.ClusterRouterGroup;
import akka.cluster.routing.ClusterRouterGroupSettings;
import akka.routing.ScatterGatherFirstCompletedGroup;
import scala.collection.JavaConversions;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Routing actor holding the actual "cluster aware" router which uses a group of routees.
 * This router automatically gets notified if a routee has left or a new one has joined the cluster
 *
 * @see <a href=""> ScatterGatherFirstCompleted routing logic</a>
 */
public class RouterActor extends AbstractLoggingActor {

    // "cluster aware router" (uses a group of local and/or remote routees, depending on the settings)
    private ActorRef router;

    static Props props() {
        return Props.create(RouterActor.class, RouterActor::new);
    }

    private RouterActor() {
    }

    @Override
    public void preStart() throws Exception {
        log().info("Router starting..");

        router = createClusterAwareRouter();

        super.preStart();
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                // forward every msg to the actual "cluster aware" router
                .matchAny(msg -> router.forward(msg, context()))
                .build();
    }

    private ActorRef createClusterAwareRouter() {
        List<String> routeesPaths = Collections.singletonList("/user/routing/worker");
        return getContext().actorOf(
                new ClusterRouterGroup(
                        new ScatterGatherFirstCompletedGroup(routeesPaths, Duration.ofSeconds(2)),
                        ClusterRouterGroupSettings.apply(
                                3,
                                JavaConversions.asScalaBuffer(routeesPaths).toList(),
                                false))
                        .props(),
                "routerWorker");
    }
}
