package se.tink.backend.aggregation.register.serviceproviders.danskebank;

import java.security.Security;
import java.util.Arrays;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.register.serviceproviders.danskebank.jwt.QsealcJwtCreator;
import se.tink.backend.aggregation.register.serviceproviders.danskebank.rpc.RegistrationRequest;

public class DanskebankRegisterCommand {

    private static final String EIDAS_PROXY_URL =
            "https://eidas-proxy.staging.aggregation.tink.network";

    public static void main(String[] args) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        String softwareStatement = QsealcJwtCreator.create().withBody("").sign("PSDSE-FINA-44059");

        RegistrationRequest requestBody =
                new RegistrationRequest(
                        "tls_client_auth",
                        Arrays.asList("authorization_code", "client_credentials"),
                        "aggregation.production.tink.com",
                        softwareStatement);

        String reseponse =
                createHttpClient()
                        .request("https://psd2-api.danskebank.com/psd2/v1.0/thirdparty/register")
                        .type("application/json")
                        .post(String.class, requestBody);

        System.out.println(reseponse);
    }

    private static TinkHttpClient createHttpClient() {

        TinkHttpClient httpClient = new LegacyTinkHttpClient();
        httpClient.setDebugOutput(true);

        //        httpClient.disableSignatureRequestHeader();
        httpClient.disableSslVerification();

        httpClient.setEidasProxy(EidasProxyConfiguration.createLocal(EIDAS_PROXY_URL));
        httpClient.setEidasIdentity(
                new EidasIdentity("oxford-staging", "5f98e87106384b2981c0354a33b51590", ""));

        return httpClient;
    }
}
