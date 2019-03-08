package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.social.security.SocialSecurityNumber;

@JsonObject
public class AccountEntity implements GeneralAccountEntity {
    @JsonProperty private PermissionsEntity permissions;

    @JsonProperty("account_number")
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
    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(maskAccountNumber())
                .setAccountNumber(getUnformattedAccountNumber())
                .setBalance(new Amount(currency, availableBalance))
                .setAlias(nickname)
                .addAccountIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.SE, getTransferAccountNumber()))
                .addAccountIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .addHolderName(generalGetName())
                .setApiIdentifier(accountNumber)
                .build();
    }

    @JsonIgnore
    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(getTransferAccountNumber());
    }

    @JsonIgnore
    @Override
    public String generalGetBank() {
        AccountIdentifier accountIdentifier = generalGetAccountIdentifier();
        return accountIdentifier.isValid()
                ? accountIdentifier.to(SwedishIdentifier.class).getBankName()
                : null;
    }

    @JsonIgnore
    @Override
    public String generalGetName() {
        return roles.stream()
                .filter(AccountOwnerEntity::isOwner)
                .map(AccountOwnerEntity::getOwnerName)
                .findFirst()
                .orElseThrow(
                        () ->
                                new NullPointerException(
                                        NordeaSEConstants.ErrorCodes.OWNER_NOT_FOUND));
    }

    // Legacy Nordea agent set accountnumber as unique id. If it is personal number then "3300"
    // clearing number is added
    @JsonIgnore
    public String getTransferAccountNumber() {
        SocialSecurityNumber.Sweden pnr =
                new SocialSecurityNumber.Sweden(getUnformattedAccountNumber());
        if (NordeaSEConstants.TransactionalAccounts.PERSONAL_ACCOUNT.equalsIgnoreCase(productName)
                && pnr.isValid()) {
            return NordeaSEConstants.TransactionalAccounts.NORDEA_CLEARING_NUMBER
                    + getUnformattedAccountNumber();
        } else {
            return getUnformattedAccountNumber();
        }
    }

    @JsonIgnore
    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonIgnore
    public String getUnformattedAccountNumber() {
        // account number format NAID-SE-SEK-ACCOUNTNUMBER
        return accountNumber.split("-")[3];
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        switch (NordeaSEConstants.ACCOUNT_TYPE_MAPPER
                .translate(category)
                .orElse(AccountTypes.OTHER)) {
            case CHECKING:
            case OTHER:
            case SAVINGS:
                return true;
            default:
                return false;
        }
    }

    // This method used for setting uniqueId is taken from the legacy Nordea agent.
    @JsonIgnore
    private String maskAccountNumber() {
        return "************" + accountNumber.substring(accountNumber.length() - 4);
    }
}
