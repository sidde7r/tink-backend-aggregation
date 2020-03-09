package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated;

import com.google.inject.Inject;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;

public final class GeneratedValueProviderImpl implements GeneratedValueProvider {

    private final LocalDateTimeSource localDateTimeSource;
    private final RandomValueGenerator randomValueGenerator;

    @Inject
    public GeneratedValueProviderImpl(
            LocalDateTimeSource localDateTimeSource, RandomValueGenerator randomValueGenerator) {
        this.localDateTimeSource = localDateTimeSource;
        this.randomValueGenerator = randomValueGenerator;
    }

    @Override
    public LocalDateTimeSource getLocalDateTimeSource() {
        return localDateTimeSource;
    }

    @Override
    public RandomValueGenerator getRandomValueGenerator() {
        return randomValueGenerator;
    }
}
