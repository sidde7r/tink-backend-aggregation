package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated;

import com.google.inject.Inject;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.uuid.UUIDSource;

public final class GeneratedValueProviderImpl implements GeneratedValueProvider {

    private final LocalDateTimeSource localDateTimeSource;
    private final UUIDSource uuidSource;

    @Inject
    public GeneratedValueProviderImpl(
            LocalDateTimeSource localDateTimeSource, UUIDSource uuidSource) {
        this.localDateTimeSource = localDateTimeSource;
        this.uuidSource = uuidSource;
    }

    @Override
    public LocalDateTimeSource getLocalDateTimeSource() {
        return localDateTimeSource;
    }

    @Override
    public UUIDSource getUuidSource() {
        return uuidSource;
    }
}
