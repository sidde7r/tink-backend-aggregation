package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    @JsonProperty private PermissionsEntity permissions;

    @JsonProperty("account_id")
    private String accountNumber;

    @JsonProperty("display_account_number")
    private String accountId;

    @JsonProperty private String iban;
    @JsonProperty private String bic;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty private String nickname;

    @JsonProperty("product_type")
    private String productType;

    @JsonProperty private String category;

    @JsonProperty("account_status")
    private String accountStatus;

    @JsonProperty("booked_balance")
    private double bookedBalance;

    @JsonProperty("credit_limit")
    private double creditLimit;

    @JsonProperty("available_balance")
    private double availableBalance;

    @JsonProperty private String currency;
    @JsonProperty private List<AccountOwnerEntity> roles;

    @JsonIgnore
    public PermissionsEntity getPermissions() {
        return permissions;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {

        TransactionalAccountType accountType = getTinkAccountType();
        TransactionalBuildStep transactionalBuildStep =
                TransactionalAccount.nxBuilder()
                        .withType(accountType)
                        .withInferredAccountFlags()
                        .withBalance(
                                BalanceModule.of(
                                        ExactCurrencyAmount.of(availableBalance, currency)))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(getIbanFormatted())
                                        .withAccountNumber(accountId)
                                        .withAccountName(nickname)
                                        .addIdentifier(new IbanIdentifier(getIbanFormatted()))
                                        .build())
                        .setApiIdentifier(accountNumber);

        return transactionalBuildStep.build();
    }

    private String getIbanFormatted() {
        return iban.replace(" ", "");
    }

    private TransactionalAccountType getTinkAccountType() {
        return TransactionalAccountType.from(
                        NordeaSEConstants.ACCOUNT_TYPE_MAPPER
                                .translate(category)
                                .orElse(AccountTypes.OTHER))
                .orElse(TransactionalAccountType.OTHER);
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        switch (getTinkAccountType()) {
            case CHECKING:
            case OTHER:
            case SAVINGS:
                return true;
            default:
                return false;
        }
    }
}
