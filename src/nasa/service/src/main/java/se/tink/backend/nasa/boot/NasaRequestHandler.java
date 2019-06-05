package se.tink.backend.nasa.boot;

import com.google.common.collect.ImmutableSet;
import org.apache.http.HttpResponse;
import se.tink.backend.nasa.boot.rpc.Credentials;
import se.tink.backend.nasa.boot.rpc.Provider;
import se.tink.backend.nasa.boot.rpc.RefreshInformationRequest;
import se.tink.backend.nasa.boot.rpc.RefreshableItem;
import se.tink.backend.nasa.boot.rpc.User;
import se.tink.backend.nasa.boot.rpc.UserProfile;
import spark.Request;
import spark.Response;

public class NasaRequestHandler implements NasaApi {
    private final String PROVIDER_NAME = "se-test-demofinancialinstitution-mobilebanking-password";
    private final String API_CLIENT_KEY = "00000000-0000-0000-0000-000000000000";

    private NasaRequestHandler() {}

    public static NasaRequestHandler CreateNasaRequestHandler() {
        return new NasaRequestHandler();
    }

    @Override
    public String ping(Request request, Response response) {
        return "pong";
    }

    @Override
    public HttpResponse initiate(Request request, Response response) throws Exception {
        User user = new User();
        UserProfile userProfile = new UserProfile();
        userProfile.setLocale("en_GB");
        user.setProfile(userProfile);

        Provider provider = new Provider();
        provider.setName(PROVIDER_NAME);
        provider.setClassName(
                "nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionAgent");
        provider.setMarket("SE");

        Credentials credentials = new Credentials();
        credentials.setProviderName(PROVIDER_NAME);
        credentials.setUsername("user");
        credentials.setPassword("password");

        RefreshInformationRequest refreshInformationRequest =
                new RefreshInformationRequest(user, provider, credentials, true);
        refreshInformationRequest.setItemsToRefresh(
                ImmutableSet.of(RefreshableItem.CHECKING_ACCOUNTS));

        return AggregationClient.refreshInformation(API_CLIENT_KEY, refreshInformationRequest);
    }
}
