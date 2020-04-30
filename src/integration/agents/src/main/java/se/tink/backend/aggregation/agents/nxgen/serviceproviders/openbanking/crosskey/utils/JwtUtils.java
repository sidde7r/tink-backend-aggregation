package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject.JwtAuthPayload;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public final class JwtUtils {

    private JwtUtils() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static String toOidcBase64(
            InternalEidasProxyConfiguration eidasProxyConfiguration,
            JwtHeader jwtHeader,
            EidasIdentity eidasIdentity,
            JwtAuthPayload jwtPayload) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            QsealcSigner signer =
                    QsealcSignerImpl.build(
                            eidasProxyConfiguration, QsealcAlg.EIDAS_RSA_SHA256, eidasIdentity);

            String jwtHeaderJson = mapper.writeValueAsString(jwtHeader);
            String jwtPayloadJson = mapper.writeValueAsString(jwtPayload);

            String base64encodedHeader =
                    Base64.getUrlEncoder().encodeToString(jwtHeaderJson.getBytes());
            String base64encodedPayload =
                    Base64.getUrlEncoder().encodeToString(jwtPayloadJson.getBytes());

            String toBeSignedPayload =
                    String.format("%s.%s", base64encodedHeader, base64encodedPayload);

            byte[] signedPayload = signer.getSignature(toBeSignedPayload.getBytes());

            String signedAndEncodedPayload = Base64.getUrlEncoder().encodeToString(signedPayload);

            return String.format("%s.%s", toBeSignedPayload, signedAndEncodedPayload);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
