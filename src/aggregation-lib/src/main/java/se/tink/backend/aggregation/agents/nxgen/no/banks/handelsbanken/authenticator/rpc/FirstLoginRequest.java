package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.LogInRequestConstants;

public class FirstLoginRequest {

    public static String build(String evryToken) {
        return "{\"credentials\":"
                + "{\"token\":\"" + evryToken.replaceAll("/", "\\\\/") + "\",\"tokenType\":\"" + LogInRequestConstants
                .TOKEN_TYPE + "\"},"
                + "\"tokenProtocolVersions\":[\"" + LogInRequestConstants.TOKEN_PROTOCOL_VERSION + "\"]}";
    }

}
