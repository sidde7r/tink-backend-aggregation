package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.NonValidIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("availableBalance")
    private BigDecimal availableBalance;

    private BigDecimal creditLimit;

    @JsonProperty("bookedBalance")
    private BigDecimal bookedBalance;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("description")
    private String description;

    @JsonProperty("id")
    private String id;

    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("holderType")
    private String holderType;

    private List<DemobankAccountIdentifier> accountIdentifiers;

    @JsonIgnore
    public TransactionalAccount toTinkAccount(List<AccountHolder> accountHolders) {
        return TransactionalAccount.nxBuilder()
                .withType(getAccountType())
                .withPaymentAccountFlag()
                .withBalance(getBalancesModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(description)
                                .addIdentifiers(getIdentifiers())
                                .build())
                .setHolderType(getAccountHolderType())
                .addParties(
                        accountHolders.stream()
                                .map(AccountHolder::getName)
                                .map(name -> new Party(name, Party.Role.HOLDER))
                                .collect(Collectors.toList()))
                .setApiIdentifier(getId())
                .build()
                .get();
    }

    @JsonIgnore
    private List<AccountIdentifier> getIdentifiers() {
        List<AccountIdentifier> validIdentifiers =
                Optional.ofNullable(accountIdentifiers).orElse(Collections.emptyList()).stream()
                        .map(this::toTinkAccountIdentifier)
                        .filter(AccountIdentifier::isValid)
                        .collect(Collectors.toList());
        if (validIdentifiers.size() > 0) {
            return validIdentifiers;
        } else {
            return Collections.singletonList(new IbanIdentifier(accountNumber));
        }
    }

    private AccountIdentifier toTinkAccountIdentifier(DemobankAccountIdentifier id) {
        try {
            return AccountIdentifier.create(
                    AccountIdentifier.Type.valueOf(id.getType()), id.getIdentifier());
        } catch (RuntimeException e) {
            return new NonValidIdentifier(id.getIdentifier());
        }
    }

    private BalanceModule getBalancesModule() {
        BalanceBuilderStep balanceBuilder = BalanceModule.builder().withBalance(getBalance());
        Optional.ofNullable(availableBalance)
                .map(b -> ExactCurrencyAmount.of(b, currency))
                .ifPresent(balanceBuilder::setAvailableBalance);
        Optional.ofNullable(creditLimit)
                .map(b -> ExactCurrencyAmount.of(b, currency))
                .ifPresent(balanceBuilder::setCreditLimit);
        return balanceBuilder.build();
    }

    @JsonIgnore
    public CreditCardAccount toTinkCreditCardAccount(List<AccountHolder> accountHolders) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(accountNumber)
                                .withBalance(getBalance())
                                .withAvailableCredit(getAvailableCredit())
                                .withCardAlias(accountNumber)
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(description)
                                .addIdentifiers(getIdentifiers())
                                .build())
                .setHolderType(getAccountHolderType())
                .addParties(
                        accountHolders.stream()
                                .map(AccountHolder::getName)
                                .map(name -> new Party(name, Party.Role.HOLDER))
                                .collect(Collectors.toList()))
                .setApiIdentifier(getId())
                .build();
    }

    private AccountHolderType getAccountHolderType() {
        return DemobankConstants.HOLDER_TYPE_TYPE_MAPPER.translate(holderType).orElse(null);
    }

    @JsonIgnore
    private ExactCurrencyAmount getAvailableCredit() {
        BigDecimal availableCredit = BigDecimal.ZERO;
        if (Objects.nonNull(creditLimit)) {
            if (bookedBalance.compareTo(BigDecimal.ZERO) < 0) {
                availableCredit = creditLimit.add(bookedBalance);
            } else {
                availableCredit = creditLimit;
            }
        }
        return ExactCurrencyAmount.of(availableCredit, currency);
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        return ExactCurrencyAmount.of(bookedBalance, currency);
    }

    @JsonIgnore
    private TransactionalAccountType getAccountType() {
        if (AccountTypes.CHECKING.equalsIgnoreCase(accountType)) {
            return TransactionalAccountType.CHECKING;
        } else return TransactionalAccountType.SAVINGS;
    }

    @JsonIgnore
    public boolean isNotCreditCard() {
        return !accountType.equalsIgnoreCase(AccountTypes.CREDIT_CARD);
    }

    @JsonIgnore
    public boolean isCreditCard() {
        return accountType.equalsIgnoreCase(AccountTypes.CREDIT_CARD);
    }

    @JsonIgnore
    public String getId() {
        return id;
    }
}
