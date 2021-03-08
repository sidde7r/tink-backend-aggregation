package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountResponse {

    private String id;

    private double availableBalance;

    private double usableBalance;

    private double bankBalance;

    private String iban;

    private String bic;

    private String bankName;

    private boolean seized;

    private String currency;

    public String getId() {
        return id;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public double getUsableBalance() {
        return usableBalance;
    }

    public double getBankBalance() {
        return bankBalance;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public String getBankName() {
        return bankName;
    }

    public boolean isSeized() {
        return seized;
    }

    public ExactCurrencyAmount getTinkBalance() {
        return ExactCurrencyAmount.of(availableBalance, currency);
    }

    public Optional<TransactionalAccount> toTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getTinkBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getIban())
                                .withAccountNumber(getIban())
                                .withAccountName(getBankName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN, getIban()))
                                .build())
                .build();
    }
}
