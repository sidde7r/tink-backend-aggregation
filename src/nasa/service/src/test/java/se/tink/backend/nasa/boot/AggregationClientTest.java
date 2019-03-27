package se.tink.backend.nasa.boot;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import org.apache.http.HttpResponse;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.nasa.boot.rpc.Credentials;
import se.tink.backend.nasa.boot.rpc.Provider;
import se.tink.backend.nasa.boot.rpc.RefreshInformationRequest;
import se.tink.backend.nasa.boot.rpc.RefreshableItem;
import se.tink.backend.nasa.boot.rpc.User;
import se.tink.backend.nasa.boot.rpc.UserProfile;

import static org.assertj.core.api.Assertions.assertThat;

public class AggregationClientTest {

    private static final String PROVIDER_NAME = "se-test-demo-fake-bank";
    private static final String API_CLIENT_KEY = "00000000-0000-0000-0000-000000000000";

    // To get a response from aggregation, configure NASA as the aggregation controller in
    // aggregation.
    // Start up NASA with ssl disabled.
    // This will enable aggregation to communicate back to NASA.
    @Ignore("This test requires having aggregation running locally.")
    @Test
    public void testAggregationRefresh()
            throws IOException, NoSuchAlgorithmException, KeyStoreException,
                    KeyManagementException {

        User user = new User();
        UserProfile userProfile = new UserProfile();
        userProfile.setLocale("en_GB");
        user.setProfile(userProfile);

        Provider provider = new Provider();
        provider.setName(PROVIDER_NAME);
        provider.setClassName("nxgen.demo.banks.demofakebank.DemoFakeBankAgent");
        provider.setMarket("SE");

        Credentials credentials = new Credentials();
        credentials.setProviderName(PROVIDER_NAME);
        credentials.setUsername("user");
        credentials.setPassword("password");
        // credentialsID
        // userID

        RefreshInformationRequest refreshInformationRequest =
                new RefreshInformationRequest(user, provider, credentials, true);
        refreshInformationRequest.setItemsToRefresh(
                ImmutableSet.of(RefreshableItem.CHECKING_ACCOUNTS));

        HttpResponse httpResponse = null;

        try {
            httpResponse =
                    AggregationClient.refreshInformation(
                            API_CLIENT_KEY, // TODO: Fetch apiKey from config
                            refreshInformationRequest);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw e;
        }

        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(204);
    }
}
