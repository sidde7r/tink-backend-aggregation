package se.tink.backend.aggregation.register.fi.opbank.utils;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import java.util.UUID;
import net.minidev.json.JSONObject;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidas.EidasProxyConstants;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;
import se.tink.backend.aggregation.eidas.Signer;
import se.tink.backend.aggregation.register.fi.opbank.OPBankRegisterConstants;
import se.tink.backend.aggregation.register.fi.opbank.OPBankRegisterConstants.Option;
import se.tink.backend.aggregation.register.fi.opbank.entities.SoftwareStatement;
import se.tink.backend.aggregation.register.fi.opbank.rpc.SsaRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class PSD2Utils {

    public static EidasProxyConfiguration eidasProxyConf =
            EidasProxyConfiguration.createLocal(OPBankRegisterConstants.Url.EIDAS_PROXY_URL.get());

    public static String generateSignedSSAJwt() {
        final JSONObject ssaJson = SoftwareStatement.create();
        final String serializedSsa = SerializationUtils.serializeToString(ssaJson);

        final Payload payload = new Payload(serializedSsa);
        final JWSObject jws = new JWSObject(createHeaderJwt(), payload);

        final String signedSSa = buildSignedJwt(jws);
        final JSONObject request = SsaRequest.create(signedSSa);
        final String jwsString = SerializationUtils.serializeToString(request);

        final Payload payload2 = new Payload(jwsString);
        final JWSObject jws2 = new JWSObject(createHeaderJwt(), payload2);

        final String signedRequest = buildSignedJwt(jws2);
        return signedRequest;
    }

    private static String buildSignedJwt(JWSObject jwsObject) {
        final Signer signer =
                new QsealcEidasProxySigner(
                        eidasProxyConf,
                        Option.CERTIFICATE_ID,
                        EidasProxyConstants.Algorithm.EIDAS_JWS_PS256);

        final String json = new Gson().toJson(jwsObject);
        final byte[] signatureBytes = signer.getSignature(json.getBytes(Charsets.UTF_8));
        return new String(signatureBytes);
    }

    private static JWSHeader createHeaderJwt() {
        return new JWSHeader.Builder(JWSAlgorithm.PS256)
                .type(JOSEObjectType.JWT)
                .keyID(Option.KEY_ID)
                .build();
    }

    public static String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

    public static int generateCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    private PSD2Utils() {
        throw new AssertionError();
    }
}
