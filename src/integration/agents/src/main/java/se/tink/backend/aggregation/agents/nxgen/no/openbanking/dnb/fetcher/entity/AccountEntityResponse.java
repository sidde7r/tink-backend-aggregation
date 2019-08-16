package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.BalanceTypes;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.Balance;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.BalancesResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntityResponse {
    private String bban;
    private String name;
    private String currency;

    public String getBban() {
        return bban;
    }

    public Optional<TransactionalAccount> toTinkAccount(final BalancesResponse balancesResponse) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getAmount(balancesResponse)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(bban)
                                .withAccountNumber(bban)
                                .withAccountName(name)
                                .addIdentifier(AccountIdentifier.create(Type.NO, bban))
                                .build())
                .setApiIdentifier(bban)
                .setBankIdentifier(bban)
                .build();
    }

    private Amount getAmount(final BalancesResponse balancesResponse) {
        final Balance balance =
                balancesResponse.getBalances().stream()
                        .filter(this::isUsableBalanceType)
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalStateException(ErrorMessages.WRONG_BALANCE_TYPE));

        return new Amount(currency, Double.valueOf(balance.getBalanceAmount().getAmount()));
    }

    private boolean isUsableBalanceType(final Balance balance) {
        return balance.getBalanceType().equalsIgnoreCase(BalanceTypes.EXPECTED);
    }
}
