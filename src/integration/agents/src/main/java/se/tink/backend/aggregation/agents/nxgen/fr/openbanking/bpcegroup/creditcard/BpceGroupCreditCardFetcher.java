package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.creditcard;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.base.BpceGroupBaseAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.creditcard.converter.BpceGroupCreditCardConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts.BalanceEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class BpceGroupCreditCardFetcher extends BpceGroupBaseAccountFetcher<CreditCardAccount> {

    public BpceGroupCreditCardFetcher(BpceGroupApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected boolean accountFilterPredicate(AccountEntity accountEntity) {
        return accountEntity.isCard();
    }

    @Override
    protected Optional<CreditCardAccount> map(
            AccountEntity accountEntity, List<BalanceEntity> balances) {
        return Optional.of(
                BpceGroupCreditCardConverter.toCreditCardAccount(accountEntity, balances));
    }
}
