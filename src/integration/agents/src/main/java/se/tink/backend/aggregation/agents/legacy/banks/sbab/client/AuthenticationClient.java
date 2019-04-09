package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.Client;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.InitBankIdResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.PollBankIdResponse;
import se.tink.backend.aggregation.log.AggregationLogger;

public class AuthenticationClient extends SBABClient {

    private static final AggregationLogger log = new AggregationLogger(AuthenticationClient.class);

    public static ImmutableMap<String, BankIdStatus> BANKID_STATUS =
            ImmutableMap.<String, BankIdStatus>builder()
                    .put("CONTINUE", BankIdStatus.WAITING)
                    .put("ONGOING", BankIdStatus.WAITING)
                    .put("SUCCESS", BankIdStatus.DONE)
                    .put("FAILURE", BankIdStatus.CANCELLED)
                    .build();

    private static final String BANKID_INIT_URL = SECURE_BASE_URL + "/auth/api/v1/initiate";
    private static final String BANKID_POLL_URL = SECURE_BASE_URL + "/auth/api/v1/pending";

    public AuthenticationClient(Client client, Credentials credentials, String userAgent) {
        super(client, credentials, userAgent);
    }

    public InitBankIdResponse initiateBankIdLogin() {
        return client.resource(BANKID_INIT_URL)
                .queryParam("dep", "privat")
                .queryParam("auth_mech", "PW_MBID")
                .queryParam("auth_device", "OTHER")
                .queryParam("pnr", credentials.getField(Field.Key.USERNAME))
                .get(InitBankIdResponse.class);
    }

    public BankIdStatus getLoginStatus(String pendingAuthCode) {
        final PollBankIdResponse response =
                client.resource(BANKID_POLL_URL)
                        .queryParam("code", pendingAuthCode)
                        .get(PollBankIdResponse.class);

        return BANKID_STATUS.getOrDefault(response.getStatus(), BankIdStatus.FAILED_UNKNOWN);
    }

    public String getBearerToken() {
        Document overview = getJsoupDocument(OVERVIEW_URL);
        Elements scriptTags = overview.select("script");
        Optional<String> token = parseBearerToken(scriptTags.html());

        if (!token.isPresent()) {
            log.error("Could not parse Bearer token from overview page");
            return null;
        }

        return token.get();
    }

    public static Optional<String> parseBearerToken(String scripts) {
        Pattern pattern = Pattern.compile("SBAB.BearerToken = '([^']+)'");
        Matcher matcher = pattern.matcher(scripts);

        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }

        return Optional.empty();
    }
}
