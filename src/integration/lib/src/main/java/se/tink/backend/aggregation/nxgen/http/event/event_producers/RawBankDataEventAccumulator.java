package se.tink.backend.aggregation.nxgen.http.event.event_producers;

import java.util.ArrayList;
import java.util.List;
import se.tink.eventproducerservice.events.grpc.RawBankDataTrackerEventProto.RawBankDataTrackerEvent;

public class RawBankDataEventAccumulator {

    private final List<RawBankDataTrackerEvent> eventList = new ArrayList<>();

    public void addEvent(RawBankDataTrackerEvent event) {
        this.eventList.add(event);
    }

    public List<RawBankDataTrackerEvent> getEventList() {
        return eventList;
    }
}
