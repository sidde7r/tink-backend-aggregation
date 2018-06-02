package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportPortfolioEvent;

public class PortfolioHistory {

    private final List<ExportPortfolioEvent> events;

    public PortfolioHistory(
            List<ExportPortfolioEvent> events) {
        this.events = events;
    }

    public List<ExportPortfolioEvent> getEvents() {
        return events;
    }
}
