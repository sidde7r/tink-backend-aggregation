package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.card;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.base.LaBanquePostaleBaseAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class LaBanquePostaleCardFetcher
        extends LaBanquePostaleBaseAccountFetcher<CreditCardAccount> {

    private final LaBanquePostaleCreditCardConverter laBanquePostaleCreditCardConverter;

    public LaBanquePostaleCardFetcher(
            LaBanquePostaleApiClient apiClient,
            LaBanquePostaleCreditCardConverter laBanquePostaleCreditCardConverter) {
        super(apiClient);
        this.laBanquePostaleCreditCardConverter = laBanquePostaleCreditCardConverter;
    }

    @Override
    protected boolean accountTypeFilterCondition(AccountEntity accountEntity) {
        return accountEntity.isCreditCard();
    }

    @Override
    protected Optional<CreditCardAccount> map(AccountEntity accountEntity) {
        return Optional.of(laBanquePostaleCreditCardConverter.toTinkCreditCard(accountEntity));
    }
}
