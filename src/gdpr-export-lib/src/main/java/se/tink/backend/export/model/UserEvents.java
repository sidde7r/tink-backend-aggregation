package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportEvent;

public class UserEvents {
    private final List<ExportEvent> events;

    public UserEvents(List<ExportEvent> events) {
        this.events = events;
    }

    public List<ExportEvent> getEvents() {
        return events;
    }
}
