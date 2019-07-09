package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.NDAPersonalNumberIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.Amount;

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

    public List<AccountOwnerEntity> getRoles() {
        return roles;
    }

    @JsonIgnore
    public PermissionsEntity getPermissions() {
        return permissions;
    }

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        AccountIdentifier identifier = getAccountIdentifier();

        IdBuildStep accountIdBuilder =
                IdModule.builder()
                        .withUniqueIdentifier(maskAccountNumber())
                        .withAccountNumber(formatAccountNumber())
                        .withAccountName(nickname)
                        .addIdentifier(identifier)
                        .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban));

        if (identifier.is(Type.SE_NDA_SSN)) {
            accountIdBuilder.addIdentifier(
                    identifier.to(NDAPersonalNumberIdentifier.class).toSwedishIdentifier());
        }
        return TransactionalAccount.nxBuilder()
                .withType(getTinkAccountType())
                .withId(accountIdBuilder.build())
                .withBalance(BalanceModule.of(new Amount(currency, availableBalance)))
                .addHolderName(generalGetName())
                .setApiIdentifier(accountNumber)
                .build();
    }

    private TransactionalAccountType getTinkAccountType() {
        return TransactionalAccountType.from(
                NordeaSEConstants.ACCOUNT_TYPE_MAPPER
                        .translate(category)
                        .orElse(AccountTypes.OTHER));
    }

    @JsonIgnore
    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        AccountIdentifier identifier = getAccountIdentifier();
        if (identifier.is(Type.SE_NDA_SSN)) {
            return identifier.to(NDAPersonalNumberIdentifier.class).toSwedishIdentifier();
        } else {
            return identifier;
        }
    }

    @JsonIgnore
    public AccountIdentifier getAccountIdentifier() {
        if (NordeaSEConstants.TransactionalAccounts.PERSONAL_ACCOUNT.equalsIgnoreCase(
                productName)) {
            AccountIdentifier ssnIdentifier =
                    AccountIdentifier.create(Type.SE_NDA_SSN, formatAccountNumber());
            if (ssnIdentifier.isValid()) {
                return ssnIdentifier;
            }
        }
        return AccountIdentifier.create(AccountIdentifier.Type.SE, formatAccountNumber());
    }

    @JsonIgnore
    @Override
    public String generalGetBank() {
        AccountIdentifier accountIdentifier = generalGetAccountIdentifier();
        if (accountIdentifier.isValid()) {
            return accountIdentifier.to(SwedishIdentifier.class).getBankName();
        }
        return null;
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

    @JsonIgnore
    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonIgnore
    public String formatAccountNumber() {
        final Pattern p = Pattern.compile(".*?([0-9]{10,})");
        // account number comes in format "NAID-SE-SEK-<ACCOUNTNUMBER>"
        Matcher matcher = p.matcher(accountNumber);
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Could not parse account number from " + accountNumber);
        }
        return matcher.group(1);
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

    // This method used for setting uniqueId is taken from the legacy Nordea agent.
    @JsonIgnore
    private String maskAccountNumber() {
        return "************" + accountNumber.substring(accountNumber.length() - 4);
    }
}
