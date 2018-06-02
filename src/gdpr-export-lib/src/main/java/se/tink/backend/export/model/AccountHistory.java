package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportAccountEvent;

public class AccountHistory {

    private final List<ExportAccountEvent> events;

    public AccountHistory(
            List<ExportAccountEvent> events) {
        this.events = events;
    }

    public List<ExportAccountEvent> getEvents() {
        return events;
    }
}
