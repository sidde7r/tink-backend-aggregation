package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;

public class OpenIdAuthorizationUtils {


    private final SoftwareStatement clientConfiguration;
    private final WellKnownResponse providerConfiguration;

    private final String state;

    public OpenIdAuthorizationUtils(SoftwareStatement clientConfiguration,
            WellKnownResponse providerConfiguration) {
        this.clientConfiguration = clientConfiguration;
        this.providerConfiguration = providerConfiguration;
        this.state = "HEJ";
    }


}
