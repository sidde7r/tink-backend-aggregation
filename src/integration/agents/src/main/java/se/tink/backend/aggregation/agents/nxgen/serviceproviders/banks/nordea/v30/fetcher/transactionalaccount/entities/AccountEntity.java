package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.ProductName;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.NDAPersonalNumberIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity implements GeneralAccountEntity {
    @JsonProperty private PermissionsEntity permissions;

    // e..g NAID-SE-SEK-920123-1234
    @JsonProperty("account_id")
    private String accountId;

    // e.g. 920123-1234
    @JsonProperty("display_account_number")
    private String displayAccountNumber;

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
    public Optional<TransactionalAccount> toTinkAccount(NordeaConfiguration nordeaConfiguration) {
        if (nordeaConfiguration.isBusinessAgent()) {
            return toBusinessTransactionalAccount();
        }
        return toPrivateTransactionalAccount();
    }

    private Optional<TransactionalAccount> toPrivateTransactionalAccount() {
        AccountIdentifier identifier = generalGetAccountIdentifier();

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
                                        .withUniqueIdentifier(maskAccountNumber())
                                        .withAccountNumber(identifier.getIdentifier())
                                        .withAccountName(nickname)
                                        .addIdentifier(identifier)
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.IBAN, iban))
                                        .build())
                        .sourceInfo(createAccountSourceInfo())
                        .setApiIdentifier(accountId)
                        .putInTemporaryStorage(NordeaBaseConstants.PROUDCT_CODE, productCode);

        if (hasAccountOwnerName()) {
            transactionalBuildStep.addHolderName(generalGetName());
        }
        return transactionalBuildStep.build();
    }

    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder()
                .bankProductCode(productCode)
                .bankAccountType(productType)
                .bankProductName(productName)
                .build();
    }

    private Optional<TransactionalAccount> toBusinessTransactionalAccount() {
        TransactionalAccountType accountType = getTinkAccountType();
        TransactionalBuildStep transactionalBuildStep =
                TransactionalAccount.nxBuilder()
                        .withType(accountType)
                        .withInferredAccountFlags()
                        .withBalance(buildBalanceModule())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(getIbanFormatted())
                                        .withAccountNumber(displayAccountNumber)
                                        .withAccountName(nickname)
                                        .addIdentifier(new IbanIdentifier(getIbanFormatted()))
                                        .build())
                        .setApiIdentifier(accountId);

        return transactionalBuildStep.build();
    }

    private BalanceModule buildBalanceModule() {
        return BalanceModule.builder()
                .withBalance(ExactCurrencyAmount.of(availableBalance - creditLimit, currency))
                .setAvailableBalance(ExactCurrencyAmount.of(availableBalance, currency))
                .setCreditLimit(ExactCurrencyAmount.of(creditLimit, currency))
                .build();
    }

    private TransactionalAccountType getTinkAccountType() {
        return TransactionalAccountType.from(
                        NordeaBaseConstants.ACCOUNT_TYPE_MAPPER
                                .translate(category)
                                .orElse(AccountTypes.OTHER))
                .orElse(TransactionalAccountType.OTHER);
    }

    @JsonIgnore
    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        AccountIdentifier identifier = getAccountIdentifier();
        if (identifier.is(AccountIdentifierType.SE_NDA_SSN)) {
            return identifier.to(NDAPersonalNumberIdentifier.class).toSwedishIdentifier();
        } else {
            return identifier;
        }
    }

    @JsonIgnore
    public AccountIdentifier getAccountIdentifier() {
        final String formattedAccountNumber = formatAccountNumber();
        if (formattedAccountNumber.length() == NDAPersonalNumberIdentifier.LENGTH) {
            final AccountIdentifier ssnIdentifier =
                    AccountIdentifier.create(
                            AccountIdentifierType.SE_NDA_SSN, formattedAccountNumber);
            if (ssnIdentifier.isValid()) {
                return ssnIdentifier;
            }
        }
        return AccountIdentifier.create(AccountIdentifierType.SE, formattedAccountNumber);
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
                .orElse(null);
    }

    @JsonIgnore
    public String getAccountId() {
        return accountId;
    }

    @JsonIgnore
    public String formatAccountNumber() {
        final Pattern p = Pattern.compile(".*?([0-9]{10,})");
        // account number comes in format "NAID-SE-SEK-<ACCOUNTNUMBER>"
        Matcher matcher = p.matcher(accountId);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Could not parse account number from " + accountId);
        }
        return matcher.group(1);
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        if (isISKAccount()) {
            return false;
        }
        switch (getTinkAccountType()) {
            case CHECKING:
            case OTHER:
            case SAVINGS:
                return true;
            default:
                return false;
        }
    }

    /**
     * This extra check is added to ensure the we don't list ISK accounts that we get in
     * FetchAccountResponse from Nordea as Savings. COB-1176
     *
     * @return
     */
    @JsonIgnore
    private boolean isISKAccount() {
        return StringUtils.containsIgnoreCase(productName, ProductName.INVESTMENT);
    }

    // This method used for setting uniqueId is taken from the legacy Nordea agent.
    @JsonIgnore
    private String maskAccountNumber() {
        return "************" + accountId.substring(accountId.length() - 4);
    }

    private String getIbanFormatted() {
        return iban.replace(" ", "");
    }

    @JsonIgnore
    private boolean hasAccountOwnerName() {
        return roles.stream()
                .filter(AccountOwnerEntity::isOwner)
                .anyMatch(AccountOwnerEntity::hasOwnerName);
    }
}
