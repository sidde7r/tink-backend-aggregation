package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.MastercardAgreementEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class BankdataCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private final BankdataApiClient bankClient;

    public BankdataCreditCardAccountFetcher(BankdataApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<MastercardAgreementEntity> mastercardAgreements = bankClient.getAccounts().getMastercardAgreements();

        if (mastercardAgreements == null) {
            return Collections.emptyList();
        }

        return mastercardAgreements.stream()
                .map(MastercardAgreementEntity::createCreditCardAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
