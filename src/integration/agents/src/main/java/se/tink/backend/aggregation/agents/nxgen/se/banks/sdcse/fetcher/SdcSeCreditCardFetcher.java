package se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.fetcher;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcServiceConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.SdcCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.parser.SdcTransactionParser;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SdcSeCreditCardFetcher extends SdcCreditCardFetcher {
    private static final Logger log = LoggerFactory.getLogger(SdcSeCreditCardFetcher.class);

    public SdcSeCreditCardFetcher(SdcApiClient bankClient, SdcSessionStorage sessionStorage,
            SdcTransactionParser transactionParser, SdcConfiguration agentConfiguration) {
        super(bankClient, sessionStorage, transactionParser, agentConfiguration);
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CreditCardAccount> creditCards = Lists.newArrayList();

        SessionStorageAgreements agreements = getAgreements();
        for (SessionStorageAgreement agreement : agreements) {
            Optional<SdcServiceConfigurationEntity> serviceConfigurationEntity = selectAgreement(agreement, agreements);

            serviceConfigurationEntity.ifPresent(configurationEntity -> {
                // SparbankenSyd accounts (of type KKPD)
                getCreditCardFromAccounts(creditCards, agreement);
                if (creditCards.size() > 0) {
                    log.info("Fetch credit cards using: getCreditCardFromAccounts: " + creditCards.size());
                }
            });
        }

        // store updated agreements
        setAgreements(agreements);

        return creditCards;
    }

    private void getCreditCardFromAccounts(List<CreditCardAccount> creditCards,
            SessionStorageAgreement agreement) {
        FilterAccountsResponse accounts = retrieveAccounts();

        // Convert accounts of type credit card to credit card
        accounts.stream()
                .filter(SdcAccount::isCreditCardAccount)
                .forEach(account -> {
                    CreditCardAccount creditCardAccount = account.toTinkCreditCardAccount(this.agentConfiguration);

                    // return value
                    creditCards.add(creditCardAccount);

                    String creditCardAccountId = creditCardAccount.getBankIdentifier();
                    // keep BankIdentifier to be able to fetch transactions
                    this.creditCardAccounts.put(creditCardAccountId, account.getEntityKey());
                    // add credit card bankId to agreement for fetching transactions later
                    agreement.addAccountBankId(creditCardAccountId);
                });
    }
}
