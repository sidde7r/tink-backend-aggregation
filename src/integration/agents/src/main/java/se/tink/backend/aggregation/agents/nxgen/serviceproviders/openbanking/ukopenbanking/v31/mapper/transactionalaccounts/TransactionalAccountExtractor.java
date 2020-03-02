package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.transactionalaccounts;

import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.account.AccountBalanceV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.account.AccountsV31Response;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class TransactionalAccountExtractor {

    private final TransactionalAccountMapper mapper;

    public Optional<TransactionalAccount> toTransactionalAccount(
            AccountsV31Response accounts, AccountBalanceV31Response balance, String partyName) {

        // todo refactor it: its "balance steered" - this method should accept 1 account and list of
        // balances
        String balanceAccountId = balance.getData().get().get(0).getAccountId();

        return accounts.stream()
                .filter(a -> a.getAccountId().equals(balanceAccountId))
                .filter(
                        a ->
                                UkOpenBankingV31Constants.ACCOUNT_TYPE_MAPPER.isOneOf(
                                        a.getRawAccountSubType(),
                                        Arrays.asList(AccountTypes.CHECKING, AccountTypes.SAVINGS)))
                .findFirst()
                .flatMap(a -> mapper.map(a, balance.getData().get(), partyName));
    }
}
