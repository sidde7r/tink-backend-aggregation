package se.tink.backend.insights.app.generators;

import se.tink.backend.insights.core.valueobjects.UserId;

public interface InsightGenerator {
    void generateIfShould(UserId userId);
}
