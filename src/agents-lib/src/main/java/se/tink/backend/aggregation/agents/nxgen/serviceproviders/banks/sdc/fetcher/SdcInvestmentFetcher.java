package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcServiceConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SdcInvestmentFetcher extends SdcAgreementFetcher implements AccountFetcher<InvestmentAccount> {
    private static final Logger log = LoggerFactory.getLogger(SdcInvestmentFetcher.class);

    private final SdcConfiguration agentConfiguration;

    public SdcInvestmentFetcher(SdcApiClient bankClient, SdcSessionStorage sessionStorage,
            SdcConfiguration agentConfiguration) {
        super(bankClient, sessionStorage);
        this.agentConfiguration = agentConfiguration;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        try {
            if (this.agentConfiguration.canRetrieveInvestmentData()) {
                SessionStorageAgreements agreements = getAgreements();
                return agreements.stream()
                        .map(agreement -> selectAgreement(agreement, agreements))
                        .filter(SdcServiceConfigurationEntity::isInvestmentDeposit)
                        .flatMap(serviceConfiguration ->
                                this.bankClient.fetchCustodyOverview()
                                        .toInvestmentAccounts(this.bankClient)
                        )
                        .collect(Collectors.toList());
            }
        } catch (HttpResponseException e) {
            log.warn("Could not fetch custody accounts.", e);
        }

        return Collections.emptyList();
    }
}
