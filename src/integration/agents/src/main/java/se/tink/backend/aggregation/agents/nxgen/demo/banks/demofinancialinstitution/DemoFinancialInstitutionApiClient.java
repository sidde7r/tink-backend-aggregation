package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator.DemoFinancialInstitutionAuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator.rpc.DemoFinancialInstitutionAuthenticationBody;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.configuration.DemoFinancialInstitutionConfiguration;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc.DemoFinancialInstitutionAccountBody;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc.DemoFinancialInstitutionAccountsResponse;
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

    public DemoFinancialInstitutionAuthenticateResponse authenticate(
            DemoFinancialInstitutionAuthenticationBody authenticationBody) {
        return createRequest(createBaseUrl().concat(DemoFinancialInstitutionConstants.Urls.AUTHENTICATE))
                .post(DemoFinancialInstitutionAuthenticateResponse.class, authenticationBody);
    }

    private URL createBaseUrl() {
        return new URL(configuration.getBaseUrl());
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public DemoFinancialInstitutionAccountsResponse fetchAccounts(String username, String token) {
        return createRequest(createBaseUrl().concat(DemoFinancialInstitutionConstants.Urls.ACCOUNTS))
                .post(
                        DemoFinancialInstitutionAccountsResponse.class,
                        new DemoFinancialInstitutionAccountBody(username, token));
    }
}
