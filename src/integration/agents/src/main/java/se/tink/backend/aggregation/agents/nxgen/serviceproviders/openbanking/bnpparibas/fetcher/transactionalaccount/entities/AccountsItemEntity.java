package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.ResponseValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
public class AccountsItemEntity {

    private CashAccountType cashAccountType;

    private String resourceId;

    @JsonProperty("_links")
    private Href linksEntity;

    private String usage;

    private String psuStatus;

    private String name;

    private String linkedAccount;

    private String bicFi;

    private String product;

    private String currency;

    private AccountIdentificationEntity accountId;

    public CashAccountType getCashAccountType() {
        return cashAccountType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Href getLinksEntity() {
        return linksEntity;
    }

    public String getUsage() {
        return usage;
    }

    public String getPsuStatus() {
        return psuStatus;
    }

    public String getName() {
        return name;
    }

    public String getBicFi() {
        return bicFi;
    }

    public String getCurrency() {
        return currency;
    }

    public Optional<TransactionalAccount> toTinkAccount(BalanceResponse balanceResponse) {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        BnpParibasBaseConstants.ACCOUNT_TYPE_MAPPER,
                        cashAccountType.toString(),
                        TransactionalAccountType.OTHER)
                .withBalance(getBalanceModule(balanceResponse.getBalances()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountId.getIban())
                                .withAccountNumber(accountId.getIban())
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(bicFi, accountId.getIban()))
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    public CreditCardAccount toTinkCreditCard(BalanceResponse balanceResponse) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(this.accountId.getIban())
                                .withBalance(getBalanceCard(balanceResponse))
                                .withAvailableCredit(ExactCurrencyAmount.of(0, "EUR"))
                                .withCardAlias(this.name)
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(this.resourceId)
                                .withAccountNumber(this.linkedAccount)
                                .withAccountName(this.name)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                this.accountId.getIban()))
                                .setProductName(this.product)
                                .build())
                .setApiIdentifier(this.resourceId)
                .build();
    }

    private BalanceModule getBalanceModule(List<BalancesItemEntity> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance(balances));
        getAvailableBalance(balances).ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private ExactCurrencyAmount getBookedBalance(List<BalancesItemEntity> balances) {
        if (balances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine booked balance from empty list of balances.");
        }
        Optional<BalancesItemEntity> balanceEntity =
                balances.stream()
                        .filter(
                                b ->
                                        ResponseValues.BALANCE_TYPE_CLOSING.equalsIgnoreCase(
                                                b.getBalanceType()))
                        .findAny();

        if (!balanceEntity.isPresent()) {
            log.warn(
                    "Couldn't determine booked balance of known type, and no credit limit included. Defaulting to first provided balance.");
        }
        return balanceEntity
                .map(Optional::of)
                .orElseGet(() -> balances.stream().findFirst())
                .map(BalancesItemEntity::getAmountEntity)
                .map(AmountEntity::toAmount)
                .get();
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(List<BalancesItemEntity> balances) {
        return balances.stream()
                .filter(
                        b ->
                                ResponseValues.BALANCE_TYPE_EXPECTED.equalsIgnoreCase(
                                        b.getBalanceType()))
                .findAny()
                .map(BalancesItemEntity::getAmountEntity)
                .map(AmountEntity::toAmount);
    }

    private ExactCurrencyAmount getBalanceCard(BalanceResponse balanceResponse) {
        return Optional.ofNullable(
                        balanceResponse.getBalances().get(0).getAmountEntity().toAmount())
                .orElse(null);
    }

    public boolean isCreditCard() {
        return CashAccountType.CARD == cashAccountType;
    }

    public boolean isCheckingAccount() {
        return CashAccountType.CACC == cashAccountType;
    }
}
