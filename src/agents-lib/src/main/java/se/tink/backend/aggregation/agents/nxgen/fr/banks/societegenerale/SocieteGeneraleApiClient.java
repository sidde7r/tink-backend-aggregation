package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale;

import javax.ws.rs.core.MediaType;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc.LoginGridResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class SocieteGeneraleApiClient {
    private final TinkHttpClient client;

    public SocieteGeneraleApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public LoginGridResponse getLoginGrid() {
        return client.request(SocieteGeneraleConstants.Url.LOGIN_GRID)
                .get(LoginGridResponse.class);
    }

    public byte[] getLoginNumPad(String crypto) {
        return client
                .request(SocieteGeneraleConstants.Url.LOGIN_NUM_PAD
                        .queryParam(SocieteGeneraleConstants.QueryParam.CRYPTO, crypto)
                )
                .get(byte[].class);
    }
}
