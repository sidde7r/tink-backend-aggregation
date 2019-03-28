package se.tink.backend.nasa.boot;

import com.google.common.collect.ImmutableSet;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import org.apache.http.HttpResponse;
import se.tink.backend.nasa.boot.rpc.Credentials;
import se.tink.backend.nasa.boot.rpc.Provider;
import se.tink.backend.nasa.boot.rpc.RefreshInformationRequest;
import se.tink.backend.nasa.boot.rpc.RefreshableItem;
import se.tink.backend.nasa.boot.rpc.User;
import spark.Request;
import spark.Response;
public class NasaRequestHandler implements NasaApi{
    private final String PROVIDER_NAME = "se-test-demo-fake-bank";

    private NasaRequestHandler() {}

    public static NasaRequestHandler CreateNasaRequestHandler() {
        return new NasaRequestHandler();
    }

    @Override
    public Object ping(Request request, Response response) {
        return "pong";
    }

    @Override
    public Object initiate(Request request, Response response) {
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
            return httpResponse;
        } catch (Exception e) {
            return  e;
        }
    }
}
