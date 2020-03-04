package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.account.AccountBalanceV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.account.AccountsV31Response;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class CreditCardAccountExtractor {

    private final CreditCardAccountMapper creditCardAccountMapper;

    public Optional<CreditCardAccount> toCreditCardAccount(
            AccountsV31Response accounts, AccountBalanceV31Response balance, String partyName) {

        // todo refactor it: its "balance steered" - this method should accept 1 account and list of
        // balances
        String balanceAccountId = balance.getData().get().get(0).getAccountId();

        return accounts.stream()
                .filter(a -> a.getAccountId().equals(balanceAccountId))
                .filter(
                        a ->
                                UkOpenBankingV31Constants.ACCOUNT_TYPE_MAPPER.isOf(
                                        a.getRawAccountSubType(), AccountTypes.CREDIT_CARD))
                .findFirst()
                .map(a -> creditCardAccountMapper.map(a, balance.getData().get(), partyName));
    }
}
