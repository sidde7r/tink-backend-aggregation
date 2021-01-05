package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NemIdParameters {
    private final String nemIdElements;
}
