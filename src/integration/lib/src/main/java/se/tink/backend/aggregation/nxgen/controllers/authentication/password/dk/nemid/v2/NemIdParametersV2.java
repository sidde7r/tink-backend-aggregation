package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NemIdParametersV2 {
    private final String nemIdElements;
    private final String initialUrl;

    public NemIdParametersV2(String nemIdElements) {
        this(nemIdElements, NemIdConstantsV2.NEM_ID_APPLET_URL);
    }
}
