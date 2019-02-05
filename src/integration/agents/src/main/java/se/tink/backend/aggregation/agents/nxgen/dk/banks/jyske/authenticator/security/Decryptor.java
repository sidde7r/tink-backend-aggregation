package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security;

import java.util.Arrays;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeSecurityHelper;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc.NemIdResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Decryptor {
    private final Token token;

    public Decryptor(Token token) {
        this.token = token;
    }

    public <C> C read(NemIdResponse challenge, Class<C> clazz) {
        byte[] decodedChallengeBytes = JyskeSecurityHelper.base64DecodeAndDecryptAES(challenge.getData(), token);
        String serializedData =
                new String(Arrays.copyOfRange(decodedChallengeBytes, 16, decodedChallengeBytes.length),
                    JyskeConstants.CHARSET);
        return Optional.ofNullable(SerializationUtils.deserializeFromString(serializedData, clazz))
                .orElseThrow(() -> new IllegalStateException("ObjectMapper read value failed"));
    }
}
