package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security;

import java.util.Optional;
import org.apache.commons.lang.StringEscapeUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeSecurityHelper;
import se.tink.libraries.serialization.utils.SerializationUtils;

public interface Encryptable {

    static String encrypt(Token token, Encryptable encryptable) {
        return Optional.ofNullable(SerializationUtils.serializeToString(encryptable))
                .map(serialized -> new String(
                                JyskeSecurityHelper.encryptWithAESAndBase64Encode(
                                        StringEscapeUtils.unescapeJava(
                                                JyskeConstants.Crypto.AES_PADDING + serialized
                                        ).getBytes(JyskeConstants.CHARSET),
                                        token
                                )
                        )
                )
                .orElseThrow(() -> new IllegalStateException("Invalid encryptable entity json format"));
    }

}
