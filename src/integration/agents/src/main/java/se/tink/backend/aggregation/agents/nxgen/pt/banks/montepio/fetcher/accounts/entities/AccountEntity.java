package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    @JsonProperty("AvaibleBalance")
    private double avaibleBalance;

    @JsonProperty("Balance")
    private double balance;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("DisplayNumber")
    private String displayNumber;

    @JsonProperty("ExternalNumber")
    private String externalNumber;

    @JsonProperty("Id")
    private String handle; // used for transaction fetching

    @JsonProperty("IsDefault")
    private boolean isDefault;

    @JsonProperty("IsVisible")
    private boolean isVisible;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Number")
    private String number;

    @JsonProperty("Order")
    private int order;

    @JsonProperty("ProductOrder")
    private int productOrder;

    @JsonProperty("Type")
    private int type;

    public Optional<TransactionalAccount> toTinkAccount() {
        BalanceModule balanceModule =
                BalanceModule.builder()
                        .withBalance(ExactCurrencyAmount.of(avaibleBalance, currency))
                        .build();
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(number)
                        .withAccountNumber(number)
                        .withAccountName(name)
                        .addIdentifier(
                                AccountIdentifier.create(AccountIdentifier.Type.IBAN, number))
                        .build();
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(balanceModule)
                .withId(idModule)
                .putInTemporaryStorage(MontepioConstants.PropertyKeys.HANDLE, handle)
                .build();
    }

    public String getHandle() {
        return handle;
    }

    public String getNumber() {
        return number;
    }
}
