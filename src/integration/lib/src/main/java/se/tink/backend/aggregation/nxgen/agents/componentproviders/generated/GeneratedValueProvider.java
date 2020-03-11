package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated;

import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;

public interface GeneratedValueProvider {

    LocalDateTimeSource getLocalDateTimeSource();

    RandomValueGenerator getRandomValueGenerator();
}
