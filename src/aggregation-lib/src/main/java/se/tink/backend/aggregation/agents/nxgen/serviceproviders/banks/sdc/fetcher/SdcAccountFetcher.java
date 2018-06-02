package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher;

import com.google.common.collect.Lists;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcServiceConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class SdcAccountFetcher extends SdcAgreementFetcher implements AccountFetcher<TransactionalAccount> {
    private final SdcConfiguration agentConfiguration;

    public SdcAccountFetcher(SdcApiClient bankClient, SdcSessionStorage sessionStorage,
            SdcConfiguration agentConfiguration) {
        super(bankClient, sessionStorage);
        this.agentConfiguration = agentConfiguration;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> accounts = Lists.newArrayList();

        SessionStorageAgreements agreements = getAgreements();

        for (SessionStorageAgreement agreement : agreements) {
            SdcServiceConfigurationEntity serviceConfiguration = selectAgreement(agreement, agreements);
            if (serviceConfiguration.isAccounts()) {
                Collection<TransactionalAccount> agreementAccounts = fetchAgreementAccounts();

                for (TransactionalAccount account : agreementAccounts) {
                    agreement.addAccountBankId(account.getBankIdentifier());
                }

                accounts.addAll(agreementAccounts);
            }
        }

        setAgreements(agreements);

        return accounts;
    }

    private Collection<TransactionalAccount> fetchAgreementAccounts() {
        FilterAccountsRequest request = new FilterAccountsRequest()
                .setIncludeCreditAccounts(true)
                .setIncludeDebitAccounts(true)
                .setOnlyFavorites(false)
                .setOnlyQueryable(true);

        return bankClient.filterAccounts(request).getTinkAccounts(agentConfiguration);
    }
}
