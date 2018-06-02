package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportInstrumentEvent;

public class InstrumentHistory {

    private final List<ExportInstrumentEvent> events;

    public InstrumentHistory(
            List<ExportInstrumentEvent> events) {
        this.events = events;
    }

    public List<ExportInstrumentEvent> getEvents() {
        return events;
    }
}
