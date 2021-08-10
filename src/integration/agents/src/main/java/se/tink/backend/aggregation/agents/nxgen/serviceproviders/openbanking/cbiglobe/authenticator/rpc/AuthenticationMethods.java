package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;

public class AuthenticationMethods {

    public static String getAuthenticationMethodId(
            List<ScaMethodEntity> scaMethods, AuthenticationType authenticationType) {
        return scaMethods.stream()
                .filter(
                        scaMethod ->
                                authenticationType.name().equals(scaMethod.getAuthenticationType()))
                .findAny()
                .map(ScaMethodEntity::getAuthenticationMethodId)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "There is no %s AuthenticationType.",
                                                authenticationType)));
    }
}
