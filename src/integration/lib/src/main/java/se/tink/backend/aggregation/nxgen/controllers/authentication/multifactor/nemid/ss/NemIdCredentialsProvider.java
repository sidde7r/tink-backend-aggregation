package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;

public interface NemIdCredentialsProvider {

    NemIdCredentials getNemIdCredentials(Credentials credentials);

    static NemIdCredentialsProvider defaultProvider() {
        return credentials ->
                NemIdCredentials.builder()
                        .userId(credentials.getField(Field.Key.USERNAME))
                        .password(credentials.getField(Field.Key.PASSWORD))
                        .build();
    }
}
