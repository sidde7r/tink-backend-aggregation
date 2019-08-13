package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class JwtUtils {
    private static final SecureRandom random = new SecureRandom();

    public static Date addHours(Date input, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(input);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }

    public static String generateId() {
        byte[] id = new byte[16];
        random.nextBytes(id);
        return EncodingUtils.encodeAsBase64String(id);
    }

    public static String[] listToStringArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }

    public static String toOidcBase64(
            EidasProxyConfiguration eidasProxyConfiguration,
            String appId,
            String clusterId,
            JwtHeader jwtHeader,
            JwtPayload jwtPayload) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            QsealcSigner signer =
                    QsealcSigner.build(
                            eidasProxyConfiguration.toInternalConfig(),
                            QsealcAlg.EIDAS_RSA_SHA256,
                            appId,
                            clusterId);

            String jwtHeaderJson;
            String jwtPayloadJson;

            jwtHeaderJson = mapper.writeValueAsString(jwtHeader);
            jwtPayloadJson = mapper.writeValueAsString(jwtPayload);

            String base64encodedHeader =
                    Base64.getEncoder().encodeToString(jwtHeaderJson.getBytes());
            String base64encodedPayload =
                    Base64.getEncoder().encodeToString(jwtPayloadJson.getBytes());

            String toBeSignedPayload =
                    String.format("%s.%s", base64encodedHeader, base64encodedPayload);

            byte[] signedPayload =
                    signer.getSignature(toBeSignedPayload.getBytes(Charsets.US_ASCII));

            String signedAndEncodedPayload = Base64.getEncoder().encodeToString(signedPayload);

            return String.format("%s.%s", toBeSignedPayload, signedAndEncodedPayload);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private JwtUtils() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }
}
