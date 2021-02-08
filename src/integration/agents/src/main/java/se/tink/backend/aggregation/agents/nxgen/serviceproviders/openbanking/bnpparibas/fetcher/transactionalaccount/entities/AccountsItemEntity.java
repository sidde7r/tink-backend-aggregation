package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
                .withBalance(BalanceModule.of(getAvailableBalance(balanceResponse)))
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
                                                Type.PAYMENT_CARD_NUMBER, this.accountId.getIban()))
                                .setProductName(this.product)
                                .build())
                .setApiIdentifier(this.resourceId)
                .build();
    }

    private ExactCurrencyAmount getAvailableBalance(BalanceResponse balanceResponse) {
        List<BalancesItemEntity> balancesList =
                Optional.ofNullable(balanceResponse.getBalances()).orElse(Collections.emptyList());

        Optional<BalancesItemEntity> futureBalance =
                balancesList.stream().filter(BalancesItemEntity::isFutureBalance).findFirst();

        Optional<BalancesItemEntity> closingBalance =
                balancesList.stream().filter(BalancesItemEntity::isClosingBalance).findFirst();

        BalancesItemEntity availableBalance;
        if (futureBalance.isPresent()) {
            availableBalance = futureBalance.get();
        } else if (closingBalance.isPresent()) {
            availableBalance = closingBalance.get();
        } else {
            throw new IllegalStateException();
        }

        return availableBalance.getAmountEntity().toAmount();
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
