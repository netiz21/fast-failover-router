package ch.sbb.tms.failover;

public class MainAppNode2 {

    public static void main(String[] args) {
        System.setProperty("akka.remote.netty.tcp.port", "2553");
        System.setProperty("akka.management.http.bind-port", "8553");

        MainAppNode1.main(args);
    }
}
