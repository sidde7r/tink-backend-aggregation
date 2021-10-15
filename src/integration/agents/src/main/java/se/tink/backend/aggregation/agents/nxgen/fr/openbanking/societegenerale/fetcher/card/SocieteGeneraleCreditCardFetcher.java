package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.card;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.base.SocieteGeneraleBaseAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities.AccountsItemEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SocieteGeneraleCreditCardFetcher
        extends SocieteGeneraleBaseAccountFetcher<CreditCardAccount> {

    public SocieteGeneraleCreditCardFetcher(SocieteGeneraleApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected boolean accountFilterCondition(AccountsItemEntity accountEntity) {
        return accountEntity.isCreditCard();
    }

    @Override
    protected Optional<CreditCardAccount> map(AccountsItemEntity accountEntity) {
        return accountEntity.toTinkCreditCard();
    }
}
