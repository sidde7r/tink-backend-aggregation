package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.LoginDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.OtpChallengeAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.OtpChallengeAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.OtpDevicePinningRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.OtpDevicePinningResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc.AccountsSummaryResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.agents.rpc.Credentials;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;

public class AktiaApiClient {
    private final TinkHttpClient client;
    private final Credentials credentials;

    private AktiaApiClient(TinkHttpClient client, Credentials credentials) {
        this.client = client;
        this.credentials = credentials;
    }

    public static AktiaApiClient createApiClient(TinkHttpClient client, Credentials credentials) {
        return new AktiaApiClient(client, credentials);
    }

    // == START Login ==
    public void authenticate(String username, String password) {
        MultivaluedMap<String, String> request = new MultivaluedMapImpl();
        request.put("grant_type", Collections.singletonList("password"));
        request.put("scope", Collections.singletonList("aktiaUser"));
        request.put("password", Collections.singletonList(password));
        request.put("username", Collections.singletonList(username));

        AuthenticationResponse response = client.request(AktiaConstants.Url.AUTHENTICATE)
                .header("Authorization", AktiaConstants.Session.AUTHORIZATION_HEADER)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(AuthenticationResponse.class, request);

        // Set the Authorization header for the session
        client.addPersistentHeader("Authorization", response.getTokenType() + " " + response.getAccessToken());
    }

    public LoginDetailsResponse loginDetails() {
        return client.request(AktiaConstants.Url.LOGIN_CHALLENGE)
                .get(LoginDetailsResponse.class);
    }

    public OtpChallengeAuthenticationResponse finalizeChallenge(OtpChallengeAuthenticationRequest request) {
        return client.request(AktiaConstants.Url.FINALIZE_CHALLENGE)
                .post(OtpChallengeAuthenticationResponse.class, request);
    }
    // == END Login ==

    // == START Pinning ==
    public OtpDevicePinningResponse deviceRegistration(OtpDevicePinningRequest request) {
        return client.request(AktiaConstants.Url.DEVICE_REGISTRATION)
                .post(OtpDevicePinningResponse.class, request);
    }
    // == END Pinning ==

    // == START Accounts ==
    public AccountsSummaryResponse accountsSummary() {
        return client.request(AktiaConstants.Url.SUMMARY)
                .get(AccountsSummaryResponse.class);
    }

    public AccountDetailsResponse accountDetails(String accountId) {
        return client.request(AktiaConstants.Url.ACCOUNT_DETAILS.parameter("accountId", accountId))
                .get(AccountDetailsResponse.class);
    }

    public String transactions(String accountId) {
        return client.request(AktiaConstants.Url.TRANSACTIONS.parameter("accountId", accountId))
                .get(String.class);
    }

    public String productsSummary() {
        return client.request(AktiaConstants.Url.SUMMARY_2)
                .get(String.class);
    }
    // == END Accounts ==

    // == START Investments ==
    public String investments() {
        return client.request(AktiaConstants.Url.INVESTMENTS)
                .get(String.class);
    }
    // == END Investments ==
}
