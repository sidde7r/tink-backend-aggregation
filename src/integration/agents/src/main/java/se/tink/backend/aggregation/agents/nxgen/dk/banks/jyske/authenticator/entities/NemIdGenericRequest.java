package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import java.util.Optional;
import org.apache.commons.lang3.StringEscapeUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeSecurityHelper;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Encryptable;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class NemIdGenericRequest {

    private NemIdGenericRequest() {}

    private String data;

    public static NemIdGenericRequest create(Token token, String padding, Encryptable encryptable) {
        NemIdGenericRequest request = new NemIdGenericRequest();

        request.data =
                Optional.ofNullable(SerializationUtils.serializeToString(encryptable))
                        .map(
                                serialized ->
                                        new String(
                                                JyskeSecurityHelper.encryptWithAESAndBase64Encode(
                                                        StringEscapeUtils.unescapeJava(
                                                                        padding + serialized)
                                                                .getBytes(JyskeConstants.CHARSET),
                                                        token)))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Invalid encryptable entity json format"));

        return request;
    }
}
