package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.creditcard.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class DetailedCardAccountResponse {
    private List<CardTransactionEntity> transactions;
    private DetailedCardAccountEntity cardAccount;
    private List<CardReservedTransactionEntity> reservedTransactions;
    private boolean moreTransactionsAvailable;
    protected int numberOfReservedTransactions;
    protected int numberOfTransactions;
    private LinksEntity links;

    public List<CardTransactionEntity> getTransactions() {
        return transactions;
    }

    public DetailedCardAccountEntity getCardAccount() {
        return cardAccount;
    }

    public List<CardReservedTransactionEntity> getReservedTransactions() {
        return reservedTransactions;
    }

    public boolean isMoreTransactionsAvailable() {
        return moreTransactionsAvailable;
    }

    public int getNumberOfReservedTransactions() {
        return numberOfReservedTransactions;
    }

    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public Optional<CreditCardAccount> toTinkCreditCardAccount(BankProfile bankProfile, String defaultCurrency) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(defaultCurrency));

        String currentBalance = cardAccount.getCurrentBalance();
        String reservedAmount = cardAccount.getReservedAmount();
        String availableAmount = cardAccount.getAvailableAmount();
        String creditLimit = cardAccount.getCreditLimit();
        if (cardAccount == null || currentBalance == null || reservedAmount == null || availableAmount == null) {
            return Optional.empty();
        }

        double balanceValue = StringUtils.parseAmount(currentBalance) + StringUtils.parseAmount(reservedAmount);
        Amount balance = new Amount(defaultCurrency, balanceValue);
        Amount creditLimitAmount = new Amount(defaultCurrency, StringUtils.parseAmount(creditLimit));
        Amount availableAmountValue = new Amount(defaultCurrency, StringUtils.parseAmount(availableAmount));

        return Optional.of(
                CreditCardAccount.builder(cardAccount.getCardNumber(), balance, availableAmountValue)
                        .setAccountNumber(cardAccount.getCardNumber())
                        .setName(cardAccount.getName())
                        .setHolderName(new HolderName(cardAccount.getCardHolder()))
                        .putInTemporaryStorage(SwedbankBaseConstants.StorageKey.CREDIT_CARD_RESPONSE, this)
                        .putInTemporaryStorage(SwedbankBaseConstants.StorageKey.PROFILE, bankProfile)
                        .build());
    }

    public List<CreditCardTransaction> toTransactions(CreditCardAccount creditCardAccount, String defaultCurrency) {
        if (transactions == null) {
            return Collections.emptyList();
        }

        return transactions.stream()
                .map(cardTransactionEntity ->
                        cardTransactionEntity.toTinkCreditCardTransaction(creditCardAccount, defaultCurrency))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<CreditCardTransaction> reservedTransactionsToTransactions(CreditCardAccount creditCardAccount,
            String defaultCurrency) {
        if (reservedTransactions == null) {
            reservedTransactions = Collections.emptyList();
        }

        return reservedTransactions.stream()
                .map(reservedTransaction -> reservedTransaction.toTinkCreditCardTransaction(creditCardAccount,
                        defaultCurrency))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public TransactionKeyPaginatorResponse<LinkEntity> toTransactionKeyPaginatorResponse(
            CreditCardAccount creditCardAccount, String defaultCurrency) {

        TransactionKeyPaginatorResponseImpl<LinkEntity> response = new TransactionKeyPaginatorResponseImpl<>();
        response.setNext(getNext());
        response.setTransactions(toTransactions(creditCardAccount, defaultCurrency));
        return response;
    }

    public LinkEntity getNext() {
        return Optional.ofNullable(links)
                .map(LinksEntity::getNext)
                .orElse(null);
    }
}
