package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcServiceConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.AccountNumberToIbanConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class SdcAccountFetcher extends SdcAgreementFetcher
        implements AccountFetcher<TransactionalAccount> {

    private final AccountNumberToIbanConverter converter;

    public SdcAccountFetcher(
            SdcApiClient bankClient,
            SdcSessionStorage sessionStorage,
            AccountNumberToIbanConverter converter) {
        super(bankClient, sessionStorage);
        this.converter = converter;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> accounts = new ArrayList<>();

        SessionStorageAgreements agreements = getAgreements();

        for (SessionStorageAgreement agreement : agreements) {
            Optional<SdcServiceConfigurationEntity> serviceConfiguration =
                    selectAgreement(agreement, agreements);

            serviceConfiguration.ifPresent(
                    configurationEntity -> {
                        if (configurationEntity.isAccounts()) {
                            Collection<TransactionalAccount> agreementAccounts =
                                    fetchAgreementAccounts();

                            for (TransactionalAccount account : agreementAccounts) {
                                agreement.addAccountBankId(account.getApiIdentifier());
                            }

                            accounts.addAll(agreementAccounts);
                        }
                    });
        }

        setAgreements(agreements);

        return accounts;
    }

    private Collection<TransactionalAccount> fetchAgreementAccounts() {
        FilterAccountsRequest request =
                new FilterAccountsRequest()
                        .setIncludeCreditAccounts(true)
                        .setIncludeDebitAccounts(true)
                        .setOnlyFavorites(false)
                        .setOnlyQueryable(true);

        try {
            return bankClient.filterAccounts(request).getTinkAccounts(converter);
        } catch (HttpResponseException e) {
            if (HttpStatus.SC_UNAUTHORIZED == e.getResponse().getStatus()) {
                log.info(
                        "[SDC] User is not authorized to fetch accounts on this agreement. Returning empty list of accounts");
                return Collections.emptyList();
            }
            throw e;
        }
    }
}
