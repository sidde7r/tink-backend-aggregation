package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportApplicationEvent;

public class ApplicationEvents {

    private final List<ExportApplicationEvent> events;

    public ApplicationEvents(
            List<ExportApplicationEvent> events) {
        this.events = events;
    }

    public List<ExportApplicationEvent> getEvents() {
        return events;
    }
}
