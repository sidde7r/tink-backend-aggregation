package se.tink.backend.aggregation.register.fi.opbank;

import com.google.common.net.HttpHeaders;
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import java.security.Security;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.register.fi.opbank.OPBankRegisterConstants.Option;
import se.tink.backend.aggregation.register.fi.opbank.utils.PSD2Utils;

public class OPBankRegisterCommand {

    public static void main(final String[] args) {

        final TinkHttpClient client = new TinkHttpClient();
        Security.addProvider(BouncyCastleProviderSingleton.getInstance());

        client.setDebugOutput(true);
        client.setEidasProxy(PSD2Utils.eidasProxyConf, Option.CERTIFICATE_ID);

        final String signJwt = PSD2Utils.generateSignedSSAJwt();

        client.request(OPBankRegisterConstants.Url.TPP_REGISTER)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, "application/jwt")
                .body(signJwt)
                .post();
    }
}
