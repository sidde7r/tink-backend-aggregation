package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils.BnpParibasUtils.getSignature;

import java.util.Arrays;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class Register {
    public static EidasIdentity defaultEidasIdentity =
            new EidasIdentity("oxford-staging", "5f98e87106384b2981c0354a33b51590", Register.class);

    public static String buildSignatureHeader(
            EidasProxyConfiguration configuration,
            BnpParibasConfiguration bnpParibasConfiguration) {
        return bnpParibasConfiguration.getKeyId()
                + BnpParibasUtils.getAlgorithm()
                + getEmptyHeaders()
                + getSignature(configuration, bnpParibasConfiguration, defaultEidasIdentity);
    }

    private static String getEmptyHeaders() {
        return String.format("%s=\"\", ", BnpParibasBaseConstants.SignatureKeys.headers);
    }

    public static RegisterRequest buildBody() {
        KeyEntity keyEntity =
                new KeyEntity(
                        Arrays.asList(
                                new KeysEntity(
                                        BnpParibasBaseConstants.RegisterUtils.CRYPT_ALG_FAMILY,
                                        "",
                                        BnpParibasBaseConstants.RegisterUtils.X5C)));
        return new RegisterRequest(
                BnpParibasBaseConstants.RegisterUtils.REDIRECT_URIS,
                BnpParibasBaseConstants.RegisterUtils.TOKEN_ENDPOINT_AUTH_METHOD,
                BnpParibasBaseConstants.RegisterUtils.GRANT_TYPES,
                BnpParibasBaseConstants.RegisterUtils.CLIENT_NAME,
                BnpParibasBaseConstants.RegisterUtils.CONTACTS,
                BnpParibasBaseConstants.RegisterUtils.PROVIDER_LEGAL_ID,
                BnpParibasBaseConstants.RegisterUtils.CONTEXT,
                BnpParibasBaseConstants.RegisterUtils.SCOPES);
    }

    public static void register(
            TinkHttpClient client,
            AgentsServiceConfiguration configuration,
            BnpParibasConfiguration bnpParibasConfiguration) {
        client.request(new URL(BnpParibasBaseConstants.RegisterUtils.REGISTER_URL))
                .accept(MediaType.APPLICATION_JSON)
                .header(
                        BnpParibasBaseConstants.RegisterUtils.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON)
                .header(
                        BnpParibasBaseConstants.RegisterUtils.SIGNATURE,
                        buildSignatureHeader(
                                configuration.getEidasProxy(), bnpParibasConfiguration))
                .post(HttpResponse.class, buildBody());
    }
}
