package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.rpc.CardDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;

public class BecCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final BecApiClient apiClient;

    public BecCreditCardFetcher(BecApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private static Predicate<AccountEntity> creditCardInAccountsList(
            CardDetailsResponse cardDetails) {
        return account -> account.getAccountId().equalsIgnoreCase(cardDetails.getAccountNumber());
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        FetchAccountResponse accounts = apiClient.fetchAccounts();

        List<CardEntity> cards = apiClient.fetchCards();
        return cards.stream()
                .filter(CardEntity::isCardActive)
                .map(card -> apiClient.fetchCardDetails(card.getUrlDetails()))
                .map(cardDetails -> createCreditCardAccount(accounts, cardDetails))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .collect(Collectors.toList());
    }

    private Optional<CreditCardAccount> createCreditCardAccount(
            FetchAccountResponse accounts, CardDetailsResponse cardDetails) {
        // Find root account for this card.
        // Filter out any accounts that do not have the correct currency.
        Optional<AccountEntity> optionalAccount =
                accounts.stream().filter(creditCardInAccountsList(cardDetails)).findFirst();
        if (!optionalAccount.isPresent()) {
            return Optional.empty();
        }
        AccountEntity account = optionalAccount.get();

        AccountDetailsResponse accountDetails =
                apiClient.fetchAccountDetails(cardDetails.getAccountNumber());

        if (!accountDetails.isCreditCardAccount()) {
            return Optional.empty();
        }

        if (Objects.isNull(accountDetails.getAccountHolder())
                || Objects.isNull(cardDetails.getCardHolderName())
                || !accountDetails
                        .getAccountHolder()
                        .equalsIgnoreCase(cardDetails.getCardHolderName())) {
            return Optional.empty();
        }

        return Optional.of(
                CreditCardAccount.builder(
                                account.getAccountId(),
                                account.getTinkBalance(),
                                accountDetails.getTinkMaxAmount())
                        .setAccountNumber(accountDetails.getAccountId())
                        .addIdentifier(new IbanIdentifier(accountDetails.getIban()))
                        .addIdentifier(new MaskedPanIdentifier(cardDetails.getCardNumber()))
                        .setHolderName(new HolderName(accountDetails.getAccountHolder()))
                        .setName(account.getAccountName())
                        .build());
    }
}
