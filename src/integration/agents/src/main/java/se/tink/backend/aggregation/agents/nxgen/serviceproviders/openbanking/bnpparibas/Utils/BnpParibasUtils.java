package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.Utils;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class BnpParibasUtils {

    private static EidasProxyConfiguration configuration;

    public BnpParibasUtils(EidasProxyConfiguration configuration) {
        this.configuration = configuration;
    }

    public static String buildSignatureHeader(EidasProxyConfiguration configuration) {
        return getKeyId() + getAlgorithm() + getEmptyHeaders() + getSignature(configuration);
    }

    public static String buildSignatureHeader(
            EidasProxyConfiguration configuration, String authorizationCode, String requestId) {
        return getKeyId()
                + getAlgorithm()
                + getHeaders()
                + getSignature(configuration, authorizationCode, requestId);
    }

    private static String getKeyId() {
        return "keyId=\"SN=6a3616cca87dc6204f498a3223e59c09,CA=CN=MULTICERT Trust Services Certification Authority 002,OU=Certification Authority,O=MULTICERT - Serviços de Certificação Electrónica S.A.,C=PT\", ";
    }

    private static String getAlgorithm() {
        return "algorithm=\"rsa-sha256\", ";
    }

    private static String getEmptyHeaders() {
        return "headers=\"\", ";
    }

    private static String getHeaders() {
        return "headers=\"" + "Authorization" + " " + "x-request-id" + "\", ";
    }

    public static String getSignature(EidasProxyConfiguration configuration) {
        String empty = "";
        return "signature=\""
                + new QsealcEidasProxySigner(configuration, "Tink")
                        .getSignatureBase64(empty.getBytes())
                + "\"";
    }

    private static String getSignature(
            EidasProxyConfiguration configuration, String authorizationCode, String requestId) {
        String signatureString =
                "Authorization"
                        + ": "
                        + authorizationCode
                        + System.lineSeparator()
                        + "x-request-id"
                        + ": "
                        + requestId;
        return "signature=\""
                + new QsealcEidasProxySigner(configuration, "Tink")
                        .getSignatureBase64(signatureString.getBytes())
                + "\"";
    }

    public static RegisterRequest buildBody() {
        List<String> redirect_uris =
                Arrays.asList(
                        "https://127.0.0.1:7357/api/v1/thirdparty/callback",
                        "https://api.tink.com/api/v1/credentials/third-party/callback",
                        "https://api.tink.se/api/v1/credentials/third-party/callback",
                        "https://main.staging.oxford.tink.se/api/v1/credentials/third-party/callback");
        String token_endpoint_auth_method = "tls_client_auth";
        List<String> grant_types =
                Arrays.asList(
                        "authorization_code", "refresh_token", "client_credentials", "password");
        String client_name = "TinkTest";
        List<String> contacts = Arrays.asList("openbanking@tink.se");
        String provider_legal_id = "PSDSE-FINA-44059";
        String context = "psd2";
        String scopes = "aisp pisp";

        KeyEntity keyEntity =
                new KeyEntity(
                        Arrays.asList(
                                new KeysEntity(
                                        "RSA",
                                        "",
                                        Arrays.asList(
                                                "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"))));

        return new RegisterRequest(
                redirect_uris,
                token_endpoint_auth_method,
                grant_types,
                client_name,
                contacts,
                provider_legal_id,
                context,
                scopes);
    }

    public static void register(TinkHttpClient client, AgentsServiceConfiguration configuration) {
        client.request(new URL("https://api-psd2.bddf.bnpparibas/as/psd2/register"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header(
                        "Signature",
                        BnpParibasUtils.buildSignatureHeader(configuration.getEidasProxy()))
                .post(HttpResponse.class, BnpParibasUtils.buildBody());
    }
}
