package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.Client;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.InitBankIdResponse;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.PollBankIdResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;

public class AuthenticationClient extends SBABClient {

    private static final AggregationLogger log = new AggregationLogger(AuthenticationClient.class);

    public static ImmutableMap<String, BankIdStatus> BANKID_STATUS = ImmutableMap.<String,BankIdStatus>builder()
            .put("CONTINUE", BankIdStatus.WAITING)
            .put("ONGOING", BankIdStatus.WAITING)
            .put("SUCCESS", BankIdStatus.DONE)
            .put("FAILURE", BankIdStatus.CANCELLED)
            .build();

    private static final String BANKID_INIT_URL = SECURE_BASE_URL + "/auth/api/v1/initiate";
    private static final String BANKID_POLL_URL = SECURE_BASE_URL + "/auth/api/v1/pending";

    private String reference;

    public AuthenticationClient(Client client, Credentials credentials, String userAgent) {
        super(client, credentials, userAgent);

    }

    public void initiateBankIdLogin() {

        InitBankIdResponse response = client.resource(BANKID_INIT_URL)
                .queryParam("dep", "privat")
                .queryParam("auth_mech", "PW_MBID")
                .queryParam("pnr", credentials.getField(Field.Key.USERNAME))
                .get(InitBankIdResponse.class);

        reference = response.getPendingAuthorizationCode();
    }

    public BankIdStatus getLoginStatus() {

        PollBankIdResponse response = client.resource(BANKID_POLL_URL)
                .queryParam("code", reference)
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
