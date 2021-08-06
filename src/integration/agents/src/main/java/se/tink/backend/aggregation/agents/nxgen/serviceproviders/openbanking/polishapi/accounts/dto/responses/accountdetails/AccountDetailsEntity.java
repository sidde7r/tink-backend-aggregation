package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accountdetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.common.AccountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.common.PsuRelationsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard.CreditCardBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
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
@Slf4j
public class AccountDetailsEntity {
    private static final int NO_OF_IBAN_CHARS = 4;
    private static final int COUNTRY_CODE_LENGTH = 2;

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
            return nameAddress.getOwnerName();
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
        TransactionalBuildStep transactionalBuildStep =
                TransactionalAccount.nxBuilder()
                        .withPatternTypeAndFlagsFrom(
                                accountTypeMapper,
                                getConcatOfDescriptionAndCode(),
                                TransactionalAccountType.CHECKING)
                        .withBalance(
                                BalanceModule.builder()
                                        .withBalance(
                                                ExactCurrencyAmount.of(
                                                        ObjectUtils.firstNonNull(
                                                                bookingBalance, availableBalance),
                                                        currency))
                                        .setAvailableBalance(
                                                ExactCurrencyAmount.of(
                                                        ObjectUtils.firstNonNull(
                                                                availableBalance, bookingBalance),
                                                        currency))
                                        .build())
                        .withId(buildIdModule())
                        .setHolderType(AccountHolderType.PERSONAL)
                        .setApiIdentifier(apiAccountId)
                        .setBankIdentifier(accountNumber)
                        .sourceInfo(
                                AccountSourceInfo.builder()
                                        .bankAccountType(accountTypeName)
                                        .bankProductCode(accountType.getCode())
                                        .bankProductName(accountType.getDescription())
                                        .build());

        if (getOwnerName() != null) {
            transactionalBuildStep.addParties(new Party(getOwnerName(), getRole()));
        }

        return transactionalBuildStep.build();
    }

    private String getConcatOfDescriptionAndCode() {
        return accountType.getCode() + accountType.getDescription();
    }

    private Party.Role getRole() {
        // based on the information from bank if they do not expose information - that can be either
        // owner or authorised user.
        if (CollectionUtils.isEmpty(psuRelations)) {
            return Party.Role.UNKNOWN;
        } else {
            return getTinkRole();
        }
    }

    private Party.Role getTinkRole() {
        String typeOfRelation = psuRelations.get(0).getTypeOfRelation();
        PolishApiConstants.Accounts.HolderRole holderRole =
                Stream.of(PolishApiConstants.Accounts.HolderRole.values())
                        .filter(role -> role.name().equalsIgnoreCase(typeOfRelation))
                        .findFirst()
                        .orElse(null);
        if (holderRole == null) {
            log.warn("Unknown role: {}", typeOfRelation);
            return Party.Role.UNKNOWN;
        }
        return holderRole == PolishApiConstants.Accounts.HolderRole.OWNER
                ? Party.Role.HOLDER
                : Party.Role.AUTHORIZED_USER;
    }

    @JsonIgnore
    public CreditCardAccount toTinkCreditCardAccount() {
        CreditCardBuildStep creditCardBuildStep =
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(accountNumber)
                                        .withBalance(
                                                ExactCurrencyAmount.of(availableBalance, currency))
                                        .withAvailableCredit(
                                                ExactCurrencyAmount.zero(
                                                        currency)) // API does not return available
                                        // credit
                                        .withCardAlias(accountType.getDescription())
                                        .build())
                        .withPaymentAccountFlag()
                        .withId(buildIdModule())
                        .setHolderType(AccountHolderType.PERSONAL)
                        .setApiIdentifier(apiAccountId)
                        .setBankIdentifier(accountNumber)
                        .sourceInfo(
                                AccountSourceInfo.builder()
                                        .bankAccountType(accountTypeName)
                                        .bankProductCode(accountType.getCode())
                                        .bankProductName(accountType.getDescription())
                                        .build());
        if (getOwnerName() != null) {
            creditCardBuildStep.addParties(new Party(getOwnerName(), getRole()));
        }
        return creditCardBuildStep.build();
    }

    private IdModule buildIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(accountNumber)
                .withAccountNumber(getNRB())
                .withAccountName(
                        ObjectUtils.firstNonNull(
                                Strings.emptyToNull(accountNameClient),
                                Strings.emptyToNull(accountTypeName)))
                .addIdentifiers(getIdentifiers())
                .build();
    }

    private ImmutableList<AccountIdentifier> getIdentifiers() {
        return ImmutableList.of(new IbanIdentifier(accountNumber), new BbanIdentifier(getBban()));
    }

    private String getNRB() {
        return accountNumber.substring(COUNTRY_CODE_LENGTH);
    }

    private String getBban() {
        return accountNumber.substring(NO_OF_IBAN_CHARS);
    }
}
