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
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.register.fi.opbank.OPBankRegisterCommand;
import se.tink.backend.aggregation.register.fi.opbank.OPBankRegisterConstants;
import se.tink.backend.aggregation.register.fi.opbank.OPBankRegisterConstants.Option;
import se.tink.backend.aggregation.register.fi.opbank.entities.SoftwareStatement;
import se.tink.backend.aggregation.register.fi.opbank.rpc.SsaRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class PSD2Utils {

    public static EidasProxyConfiguration eidasProxyConf =
            EidasProxyConfiguration.createLocal(OPBankRegisterConstants.Url.EIDAS_PROXY_URL.get());

    public static String generateSignedSSAJwt(
            String certificateId, String clusterId, String appId) {
        final JSONObject ssaJson = SoftwareStatement.create();
        final String serializedSsa = SerializationUtils.serializeToString(ssaJson);

        final Payload payload = new Payload(serializedSsa);
        final JWSObject jws = new JWSObject(createHeaderJwt(), payload);

        final String signedSSa = buildSignedJwt(jws, certificateId, clusterId, appId);
        final JSONObject request = SsaRequest.create(signedSSa);
        final String jwsString = SerializationUtils.serializeToString(request);

        final Payload payload2 = new Payload(jwsString);
        final JWSObject jws2 = new JWSObject(createHeaderJwt(), payload2);

        final String signedRequest = buildSignedJwt(jws2, certificateId, clusterId, appId);
        return signedRequest;
    }

    private static String buildSignedJwt(
            JWSObject jwsObject, String certificateId, String clusterId, String appId) {
        EidasIdentity eidasIdentity =
                new EidasIdentity(clusterId, appId, OPBankRegisterCommand.class);

        QsealcSigner signer =
                QsealcSignerImpl.build(
                        eidasProxyConf.toInternalConfig(),
                        QsealcAlg.EIDAS_JWS_PS256,
                        eidasIdentity,
                        certificateId);

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
