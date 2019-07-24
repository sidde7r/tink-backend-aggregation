package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class BankAccountsEntity {
    @JsonProperty("Balance")
    private BalanceEntity balance;

    @JsonProperty("Currency")
    private int currency;

    @JsonProperty("DisplayTypeName")
    private String displayTypeName = "";

    @JsonProperty("DisplayNumber")
    private String displayNumber = "";

    @JsonProperty("EncryptedNumber")
    private String encryptedNumber = "";

    @JsonProperty("Holder")
    private HolderEntity holder;

    @JsonProperty("Interest")
    private BigDecimal interest;

    @JsonProperty("LendingInterest")
    private BigDecimal lendingInterest;

    @JsonProperty("OwnedBySelf")
    private boolean ownedBySelf;

    @JsonProperty("Position")
    private int position;

    @JsonProperty("RoleType")
    private int roleType;

    @JsonProperty("RoleTypeName")
    private String roleTypeName = "";

    @JsonProperty("Status")
    private int status;

    @JsonProperty("StatusName")
    private String statusName = "";

    @JsonProperty("Type")
    private int type;

    @JsonProperty("Reference")
    private String reference = "";

    @JsonProperty("DisplayName")
    private String displayName;

    @JsonProperty("Name")
    private String name = "";

    @JsonProperty("Number")
    private String number = "";

    @JsonProperty("TypeName")
    private String typeName = "";

    @JsonIgnore
    private String getDisplayName() {
        return Optional.ofNullable(displayName).orElse(displayTypeName);
    }

    @JsonIgnore
    private AccountTypes getAccountType() {
        return SkandiaBankenConstants.ACCOUNT_TYPE_MAPPER
                .translate(typeName)
                .orElse(AccountTypes.OTHER);
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        switch (getAccountType()) {
            case CHECKING:
            case OTHER:
            case SAVINGS:
                return true;
            default:
                return false;
        }
    }

    @JsonIgnore
    public TransactionalAccount toTinkTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.from(getAccountType()))
                .withBalance(BalanceModule.of(balance.getAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(number)
                                .withAccountNumber(number)
                                .withAccountName(getDisplayName())
                                .addIdentifier(new SwedishIdentifier(number))
                                .build())
                .addHolderName(holder.getHolderName())
                .setApiIdentifier(encryptedNumber)
                .setBankIdentifier(encryptedNumber)
                .build();
    }
}
