package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated;

import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.uuid.UUIDSource;

public interface GeneratedValueProvider {

    LocalDateTimeSource getLocalDateTimeSource();

    UUIDSource getUuidSource();
}
