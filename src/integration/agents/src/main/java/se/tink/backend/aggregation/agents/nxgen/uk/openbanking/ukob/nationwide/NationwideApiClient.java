package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.nationwide;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.AccountBalanceV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class NationwideApiClient extends UkOpenBankingApiClient {
    private static final String ERROR_CODE = "UK.OBIE.Resource.NotFound";
    private static final String ERROR_MESSAGE = "Requested resource cannot be found";
    private final UkOpenBankingAisConfig aisConfig;

    public NationwideApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration,
            RandomValueGenerator randomValueGenerator,
            PersistentStorage persistentStorage,
            UkOpenBankingAisConfig aisConfig,
            AgentComponentProvider componentProvider) {
        super(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                randomValueGenerator,
                persistentStorage,
                aisConfig,
                componentProvider);
        this.aisConfig = aisConfig;
    }

    @Override
    public List<AccountBalanceEntity> fetchV31AccountBalances(String accountId) {
        try {
            return createAisRequest(aisConfig.getAccountBalanceRequestURL(accountId))
                    .get(AccountBalanceV31Response.class)
                    .getData()
                    .map(this::logBalanceSnapshotTime)
                    .orElse(Collections.emptyList());
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            // Nationwide confirmed that accounts with this error code and message are locked.
            // Ticket on service desk: OBSD-27116
            if (is400(response) && hasSpecificCodeErrorAndMessage(response)) {
                log.warn("[NationwideApiClient] Account is temporary locked.");
                return Collections.emptyList();
            }
            throw new AccountRefreshException("Failed to fetch balances.", e);
        }
    }

    private boolean is400(HttpResponse response) {
        return response.getStatus() == HttpStatus.SC_BAD_REQUEST;
    }

    private boolean hasSpecificCodeErrorAndMessage(HttpResponse response) {
        ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
        return errorResponse.hasErrorCode(ERROR_CODE)
                && errorResponse.messageContains(ERROR_MESSAGE);
    }
}
