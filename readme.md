# Fast-Failover (cluster-aware Routing)

Einfaches Bsp. wie mittels einem "cluster-aware" Router ein Fast-Failover Setup aufgesezt
werden kann.
Zentraler Teil dabei ist der RouterActor der nach ScatterGatherFirstCompleted-Logik eine Gruppe
von Routees besitzt (hier auf jedem Cluster Node einer), an die er seine Meldungen weiterleitet.
Der Routee der zuerst eine Antwort zurück liefert (schnellste Antwort) wird berücksichtigt, die anderen werden verworfen.
Dadurch haben wir sichergestellt, dass alle Routees (WorkerActor) auf jedem Cluster-Node die selben Meldungen 
erhalten und damit den selben State aufweisen sollten. 
Fällt einer der Routees weg (weil ein Node down ist), so liefert einfach ein anderer aus der Gruppe die "erste" Antwort.
Kommen neue Cluster-Nodes und damit Routees hinzu, werden diese autom. zur Router-Gruppe hinzugefügt; das selbe
passiert wenn ein Routee oder Node wegfällt.
Fazit: Damit lässt sich ziemlich einfach ein Fast-Failover erreichen, welches im Falle eines Crashs sofort auf einen
anderen verfügbaren Actor ausweicht (max. 1, 2 Sekunden).

## Vorteile
* rasches Failover im Fehlerfall
* autom. Joining / Removing der Routees durch Akka wenn Nodes hinzukommen oder wegfallen
* wenn man auf jedem Node die selben Aktoren (Routees) mit dem selben State hat, so kann man eingehende Meldungen
ebenfalls auf alle Nodes verteilen, d.h. einen Message-Listener / -Consumer pro Node führen (Meldungen werden zufällig von einem
der Nodes verarbeitet und an den Router weitergeleitet. Da alle Workers den selben State haben, spielt es keine Rolle, welcher Node die Meldung entgegen nimmt
und dann an die Routees weiterleitet -> kein Single point of Failure!) 

## Nachteile
* Meldungen werden mehrfach verarbeitet (redundante Verarbeitung auf jedem Cluster-Node)
* bedeutet auch zusätzlichen I/O (bei kleinen Meldungen nicht tragisch)

**Wichtig**: das Ganze funktioniert vorerst ohne Persistenz! Möchte man den State des Routees persistieren, so
reicht dieses Konzept alleine nicht mehr. Grund: es gibt für ein und die selbe "persistenceId" immer nur ein PersistentActor (Writer),
der die Events schreiben darf! (wegen Reihenfolge, Sequenznummer). Somit können wir aus den Routees nicht einfach "persistente" Actors machen.