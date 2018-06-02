package se.tink.backend.insights.app;

import com.google.common.collect.ImmutableList;
import se.tink.backend.insights.app.generators.InsightGenerator;
import se.tink.backend.insights.core.valueobjects.UserId;

public interface GeneratorsProvider {
    ImmutableList<InsightGenerator> getGenerators(UserId userId);
}
