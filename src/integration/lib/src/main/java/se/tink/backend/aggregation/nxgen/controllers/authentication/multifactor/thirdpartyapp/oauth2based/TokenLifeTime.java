package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based;

import java.time.temporal.TemporalUnit;
import lombok.Value;

@Value
public final class TokenLifeTime {

    private final int lifetime;
    private final TemporalUnit unit;
}
