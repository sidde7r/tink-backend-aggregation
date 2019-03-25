package se.tink.backend.nasa.boot;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.apache.http.HttpResponse;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.nasa.boot.rpc.Credentials;
import se.tink.backend.nasa.boot.rpc.Provider;
import se.tink.backend.nasa.boot.rpc.RefreshInformationRequest;
import se.tink.backend.nasa.boot.rpc.RefreshableItem;
import se.tink.libraries.user.rpc.User;

import static org.assertj.core.api.Assertions.assertThat;

public class AggregationClientTest {

    public static final String PROVIDER_NAME = "se-test-demo-fake-bank";

    @Ignore("This test requires having aggregation running locally.")
    @Test
    public void testAggregationRefresh() throws IOException {

        User user = new User();
        Provider provider = new Provider();
        provider.setName(PROVIDER_NAME);
        provider.setClassName("nxgen.demo.banks.demofakebank.DemoFakeBankAgent");

        Credentials credentials = new Credentials();
        credentials.setProviderName(PROVIDER_NAME);
        credentials.setUsername("user");
        credentials.setPassword("password");
        // credentialsID
        // userID

        RefreshInformationRequest refreshInformationRequest =
                new RefreshInformationRequest(user, provider, credentials, false);
        refreshInformationRequest.setItemsToRefresh(
                ImmutableSet.of(RefreshableItem.CHECKING_ACCOUNTS));

        HttpResponse httpResponse = null;

        try {
            httpResponse =
                    AggregationClient.refreshInformation(
                            "00000000-0000-0000-0000-000000000000",
                            refreshInformationRequest); // TODO: Fetch apiKey from config
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(204);
    }
}
