package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcAlgorithm;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject.JwtAuthPayload;

public final class JwtUtils {

    public static String toOidcBase64(
            QsealcSigner qsealcSigner, JwtHeader jwtHeader, JwtAuthPayload jwtPayload) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            String jwtHeaderJson = mapper.writeValueAsString(jwtHeader);
            String jwtPayloadJson = mapper.writeValueAsString(jwtPayload);

            String base64encodedHeader =
                    Base64.getUrlEncoder().encodeToString(jwtHeaderJson.getBytes());
            String base64encodedPayload =
                    Base64.getUrlEncoder().encodeToString(jwtPayloadJson.getBytes());

            String toBeSignedPayload =
                    String.format("%s.%s", base64encodedHeader, base64encodedPayload);

            String signedAndEncodedPayload =
                    qsealcSigner
                            .sign(QsealcAlgorithm.RSA_SHA256, toBeSignedPayload.getBytes())
                            .getBase64UrlEncoded();

            return String.format("%s.%s", toBeSignedPayload, signedAndEncodedPayload);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
