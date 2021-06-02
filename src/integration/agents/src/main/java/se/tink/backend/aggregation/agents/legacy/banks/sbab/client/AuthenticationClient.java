package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.Client;
import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.QueryParamKeys;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.QueryParamValues;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.Url;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.InitBankIdResponse;
import se.tink.backend.aggregation.agents.banks.sbab.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

@Slf4j
public class AuthenticationClient extends SBABClient {

    private static final ImmutableMap<String, BankIdStatus> BANKID_STATUS =
            ImmutableMap.<String, BankIdStatus>builder()
                    .put("CONTINUE", BankIdStatus.WAITING)
                    .put("ONGOING", BankIdStatus.WAITING)
                    .put("SUCCESS", BankIdStatus.DONE)
                    .put("FAILURE", BankIdStatus.CANCELLED)
                    .build();

    private static final String BANKID_INIT_URL = Url.BANKID_BASE_URL + "/initiate";
    private static final String BANKID_POLL_URL = Url.BANKID_BASE_URL + "/pending";

    public AuthenticationClient(Client client, Credentials credentials, String userAgent) {
        super(client, credentials, userAgent);
    }

    public InitBankIdResponse initiateBankIdLogin() {
        return client.resource(BANKID_INIT_URL)
                .queryParam(QueryParamKeys.DEP, QueryParamValues.DEP)
                .queryParam(QueryParamKeys.AUTH_MECH, QueryParamValues.AUTH_MECH)
                .queryParam(QueryParamKeys.AUTH_DEVICE, QueryParamValues.AUTH_DEVICE)
                .queryParam(QueryParamKeys.REV, String.valueOf(new Date().getTime()))
                .get(InitBankIdResponse.class);
    }

    public BankIdStatus getLoginStatus(String pendingAuthCode) {
        final PollBankIdResponse response =
                client.resource(BANKID_POLL_URL)
                        .queryParam("code", pendingAuthCode)
                        .get(PollBankIdResponse.class);

        return BANKID_STATUS.getOrDefault(response.getStatus(), BankIdStatus.FAILED_UNKNOWN);
    }

    public String getCsrfToken() throws AuthorizationException {
        Document overview = getJsoupDocument(Url.OVERVIEW_URL);
        /* TODO Add KYC check when we know how it looks*/

        Elements scriptTags = overview.select("script");
        Optional<String> token = parseCsrfToken(scriptTags.html());

        if (!token.isPresent()) {
            log.error("Could not parse CSRF token from overview page");
            return null;
        }

        return token.get();
    }

    public static Optional<String> parseCsrfToken(String scripts) {
        String[] first = scripts.split("window.csrfToken = \"");
        String token = first[1].split("\"")[0];
        if (token != "") {
            return Optional.of(token);
        }

        return Optional.empty();
    }
}
