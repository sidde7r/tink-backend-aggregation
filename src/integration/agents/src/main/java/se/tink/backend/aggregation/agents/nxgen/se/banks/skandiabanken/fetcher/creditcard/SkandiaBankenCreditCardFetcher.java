package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.entities.BankAccountsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@Slf4j
public class SkandiaBankenCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final SkandiaBankenApiClient apiClient;

    public SkandiaBankenCreditCardFetcher(SkandiaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CreditCardAccount> accountList = new ArrayList<>();

        apiClient.fetchAccounts().getAccounts().stream()
                .filter(BankAccountsEntity::isCreditCardAccount)
                .forEach(
                        accountEntity ->
                                getAccountOwnerCard(accountEntity)
                                        .ifPresent(
                                                cardEntity ->
                                                        accountList.add(
                                                                accountEntity.toCreditCardAccount(
                                                                        cardEntity))));

        return accountList;
    }

    /**
     * The credit card account is parsed as the Tink credit card account, and an account could have
     * multiple cards with different owners. Best solution with how our models look is to use the
     * account owner's card details building the Tink credit card account.
     */
    private Optional<CardEntity> getAccountOwnerCard(BankAccountsEntity accountEntity) {
        Optional<CardEntity> accountOwnerCard =
                apiClient.fetchCards().getCards().stream()
                        .filter(
                                cardEntity ->
                                        (cardBelongsToAccount(accountEntity, cardEntity)
                                                && isAccountOwnerCard(accountEntity, cardEntity)))
                        .findFirst();

        if (!accountOwnerCard.isPresent()) {
            log.warn("No card exist for account owner, credit card account will be omitted.");
        }

        return accountOwnerCard;
    }

    private boolean cardBelongsToAccount(BankAccountsEntity accountEntity, CardEntity cardEntity) {
        return accountEntity.getReference().equals(cardEntity.getBankAccountReference());
    }

    private boolean isAccountOwnerCard(BankAccountsEntity accountEntity, CardEntity cardEntity) {
        return accountEntity
                .getHolder()
                .getNationalIdentificationNumber()
                .equals((cardEntity.getHolder().getNationalIdentificationNumber()));
    }
}
