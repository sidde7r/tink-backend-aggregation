package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accountdetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.common.AccountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.common.PsuRelationsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.dto.responses.NameAddressEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDetailsEntity {
    private static final int NO_OF_IBAN_CHARS = 4;

    private String accountNumber;

    private AccountTypeEntity accountType;
    private NameAddressEntity nameAddress;
    private String accountTypeName;
    private String accountHolderType;
    private String currency;
    private String availableBalance;
    private String bookingBalance;
    private List<PsuRelationsEntity> psuRelations;
    private String vatAccountNrb;

    @JsonProperty("name")
    @Getter(value = AccessLevel.NONE)
    private String ownerName;

    private String accountNameClient;

    private String getOwnerName() {
        if (nameAddress != null) {
            return nameAddress.getValue().get(0);
        }
        return ownerName;
    }

    @JsonIgnore @Setter
    /**
     * That can be either accountNumber or internal accountIdentifier. It could be accountIdentifier
     * only if it comes from token response.
     */
    private String apiAccountId;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(AccountTypeMapper accountTypeMapper) {
        return TransactionalAccount.nxBuilder()
                .withPatternTypeAndFlagsFrom(
                        accountTypeMapper,
                        getConcatOfDescriptionAndCode(),
                        TransactionalAccountType.CHECKING)
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(ExactCurrencyAmount.of(availableBalance, currency))
                                .setAvailableBalance(
                                        ExactCurrencyAmount.of(
                                                ObjectUtils.firstNonNull(
                                                        bookingBalance, availableBalance),
                                                currency))
                                .build())
                .withId(buildIdModule())
                .addHolderName(getOwnerName())
                .setHolderType(AccountHolderType.PERSONAL)
                .setApiIdentifier(apiAccountId)
                .setBankIdentifier(accountNumber)
                .sourceInfo(
                        AccountSourceInfo.builder()
                                .bankAccountType(accountTypeName)
                                .bankProductCode(accountType.getCode())
                                .bankProductName(accountType.getDescription())
                                .build())
                .build();
    }

    private String getConcatOfDescriptionAndCode() {
        return accountType.getCode() + accountType.getDescription();
    }

    @JsonIgnore
    public CreditCardAccount toTinkCreditCardAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(accountNumber)
                                .withBalance(ExactCurrencyAmount.of(availableBalance, currency))
                                .withAvailableCredit(
                                        ExactCurrencyAmount.zero(
                                                currency)) // API does not return available credit
                                .withCardAlias(accountType.getDescription())
                                .build())
                .withPaymentAccountFlag()
                .withId(buildIdModule())
                .addHolderName(getOwnerName())
                .setHolderType(AccountHolderType.PERSONAL)
                .setApiIdentifier(apiAccountId)
                .setBankIdentifier(accountNumber)
                .sourceInfo(
                        AccountSourceInfo.builder()
                                .bankAccountType(accountTypeName)
                                .bankProductCode(accountType.getCode())
                                .bankProductName(accountType.getDescription())
                                .build())
                .build();
    }

    private IdModule buildIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(accountNumber)
                .withAccountNumber(accountNumber)
                .withAccountName(ObjectUtils.firstNonNull(accountNameClient, accountTypeName))
                .addIdentifiers(getIdentifiers())
                .build();
    }

    private ImmutableList<AccountIdentifier> getIdentifiers() {
        return ImmutableList.of(
                new IbanIdentifier(accountNumber),
                new BbanIdentifier(accountNumber.substring(NO_OF_IBAN_CHARS)));
    }
}
