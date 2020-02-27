package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper;

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

        return accounts.stream()
                .filter(e -> e.getAccountId().equals(balance.getBalance().getAccountId()))
                .filter(
                        e ->
                                UkOpenBankingV31Constants.ACCOUNT_TYPE_MAPPER.isOf(
                                        e.getRawAccountSubType(), AccountTypes.CREDIT_CARD))
                .findFirst()
                .map(e -> creditCardAccountMapper.map(e, balance.getData().get(), partyName));
    }
}
