package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.rpc.BankIdRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class SEBApiClient {
    private final TinkHttpClient httpClient;
    private final String sebUUID;

    public SEBApiClient(TinkHttpClient httpClient) {
        this.httpClient = httpClient;
        sebUUID = UUID.randomUUID().toString().toUpperCase();
    }

    public BankIdResponse fetchAutostartToken() {
        return httpClient
                .request(Urls.FETCH_AUTOSTART_TOKEN)
                .header(HeaderKeys.X_SEB_UUID, sebUUID)
                .body(new BankIdRequest(), MediaType.APPLICATION_JSON)
                .post(BankIdResponse.class);
    }

    public BankIdResponse collectBankId(final String reference) {
        return httpClient
                .request(Urls.COLLECT_BANKID.concat(reference))
                .header(HeaderKeys.X_SEB_UUID, sebUUID)
                .post(BankIdResponse.class);
    }
}
