package ch.sbb.tms.failover;

public class MainAppNode3 {

    public static void main(String[] args) {
        System.setProperty("akka.remote.netty.tcp.port", "2554");
        System.setProperty("akka.management.http.bind-port", "8554");

        MainAppNode1.main(args);
    }
}
