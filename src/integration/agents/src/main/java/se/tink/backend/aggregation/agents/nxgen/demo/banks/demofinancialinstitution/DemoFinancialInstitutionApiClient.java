package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.configuration.DemoFinancialInstitutionConfiguration;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class DemoFinancialInstitutionApiClient {
    private final TinkHttpClient client;
    private DemoFinancialInstitutionConfiguration configuration;

    public DemoFinancialInstitutionApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public DemoFinancialInstitutionConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(DemoFinancialInstitutionConfiguration configuration) {
        this.configuration = configuration;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        return createRequest(createBaseUrl().concat(Urls.LOGIN))
                .post(LoginResponse.class, loginRequest);
    }

    private URL createBaseUrl() {
        return new URL(configuration.getBaseUrl());
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public FetchAccountsResponse fetchAccounts() {
        final URL url = createBaseUrl().concat(Urls.ACCOUNTS);

        return createRequest(url).get(FetchAccountsResponse.class);
    }
}
