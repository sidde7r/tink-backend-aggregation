package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.google.common.base.Objects;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MultivaluedMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.AuthBankIdPollResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;

public class AuthenticationClient extends SBABClient {

    private static final AggregationLogger log = new AggregationLogger(AuthenticationClient.class);

    private static final String SBAB_WEBPAGE_URL = "http://www.sbab.se/";
    private static final String BANKID_LOGIN_URL = SECURE_BASE_URL + "/login/mobilt_bankid?dep=privat";
    private static final String BANKID_AUTH_URL = IDP_BASE_URL + "/wa/auth";
    private static final String BANKID_REDIRECT_START_URL =
            SECURE_BASE_URL + "/login/mobillandning?PW_AuthMech=PW_MBID&dep=privat";

    public AuthenticationClient(Client client, Credentials credentials, String userAgent) {
        super(client, credentials, userAgent);
    }

    public void initiateBankIdLogin() {
        MultivaluedMap<String, String> bankIdStartBody = createBankIdStartBody();

        setAllCookiesForMobileBankIdLogin();

        Document startLoginResponse = Jsoup.parse(createFormEncodedHtmlRequest(BANKID_AUTH_URL)
                .post(String.class, bankIdStartBody));

        // Sometimes SBAB complains about a previously initiated BankID session here. If that is the case, the problem
        // can be solved by making a second request to initiate a session.
        Element error = startLoginResponse.select("p.form-error").first();
        if (error != null) {
            log.info("Got error from SBAB, but will make a second attempt. Error: " + error.text());

            startLoginResponse = Jsoup.parse(createFormEncodedHtmlRequest(BANKID_AUTH_URL)
                    .post(String.class, bankIdStartBody));

            error = startLoginResponse.select("p.form-error").first();
            if (error != null) {
                // Todo: throw better exceptions based on `error.text()`
                throw new IllegalStateException(
                                String.format(
                                        "Made second attempt to initiate login, but failed. Error: %s",
                                        error.text()
                                )
                );
            }

            log.info("Successfully initiated login on second try");
        }
    }

    public BankIdStatus getLoginStatus() {
        try {
            MultivaluedMap<String, String> bankIdPollBody = createBankIdPollBody();
            String pollString = createFormEncodedJsonRequest(BANKID_AUTH_URL).post(String.class, bankIdPollBody);
            AuthBankIdPollResponse pollResponse = SerializationUtils
                    .deserializeFromString(pollString, AuthBankIdPollResponse.class);

            log.info("Awaiting BankID authentication. Status = " + pollResponse.getStatus());

            switch (pollResponse.getBankIdStatus()) {
            case DONE:
                MultivaluedMap<String, String> bankIdDoneBody = createBankIdDoneBody();
                ClientResponse loginResponse = client.resource(BANKID_AUTH_URL)
                        .post(ClientResponse.class, bankIdDoneBody);

                ClientResponse postSamlResponse = postSamlHtmlForm(getRedirectUrl(loginResponse, IDP_BASE_URL));
                createGetRequest(getRedirectUrl(postSamlResponse, SECURE_BASE_URL));

                log.info("Successfully logged in with BankID.");
                return BankIdStatus.DONE;

            default:
                return pollResponse.getBankIdStatus();
            }

        } catch (Exception e) {
            log.error("Received exception while trying to authenticate with BankID.", e);
            return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    private ClientResponse postSamlHtmlForm(String samlRedirectUrl) throws Exception {
        MultivaluedMapImpl samlPostBody = new MultivaluedMapImpl();
        Document samlHtml = getJsoupDocument(samlRedirectUrl);
        Element samlForm = samlHtml.getElementsByAttributeValue("name", "samlform").first();
        Element samlResponse = samlForm.select("input[name=SAMLResponse]").first();
        Element relayState = samlForm.select("input[name=RelayState]").first();
        samlPostBody.add("SAMLResponse", samlResponse.val());
        samlPostBody.add("RelayState", relayState.val());
        return createFormEncodedHtmlRequest(samlForm.attr("action")).post(ClientResponse.class, samlPostBody);
    }

    private MultivaluedMap<String, String> createBankIdDoneBody() {
        MultivaluedMapImpl bankIdDoneBody = new MultivaluedMapImpl();
        bankIdDoneBody.add("do", "done");
        bankIdDoneBody.add("authmech", "Mobilt BankID");
        return bankIdDoneBody;
    }

    private MultivaluedMap<String, String> createBankIdPollBody() {
        MultivaluedMapImpl bankIdPollBody = new MultivaluedMapImpl();
        bankIdPollBody.add("do", "poll");
        bankIdPollBody.add("authmech", "Mobilt BankID");
        return bankIdPollBody;
    }

    private MultivaluedMap<String, String> createBankIdStartBody() {
        MultivaluedMapImpl bankIdStartBody = new MultivaluedMapImpl();

        bankIdStartBody.add("authmech", "Mobilt BankID");
        bankIdStartBody.add("do", "login");

        String ssn = credentials.getField(Field.Key.USERNAME);
        if (Objects.equal(ssn.length(), 12)) {
            ssn = ssn.substring(2, ssn.length());
        }

        bankIdStartBody.add("nin", ssn);
        bankIdStartBody.add("Submit1", "Nästa steg");

        return bankIdStartBody;
    }

    private void setAllCookiesForMobileBankIdLogin() {
        createRequest(SBAB_WEBPAGE_URL)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .get(ClientResponse.class);
        createGetRequest(BANKID_LOGIN_URL);
        createGetRequest(BANKID_REDIRECT_START_URL);
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
