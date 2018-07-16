package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.rpc.CardDetailsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BecCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(BecCreditCardFetcher.class);
    private final BecApiClient apiClient;

    public BecCreditCardFetcher(BecApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private static Predicate<AccountEntity> creditCardInAccountsList(CardDetailsResponse cardDetails) {
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

    private Optional<CreditCardAccount> createCreditCardAccount(FetchAccountResponse accounts,
            CardDetailsResponse cardDetails) {
        // Find root account for this card.
        // Filter out any accounts that do not have the correct currency.
        Optional<AccountEntity> optionalAccount = accounts.stream()
                .filter(creditCardInAccountsList(cardDetails))
                .findFirst();
        if (!optionalAccount.isPresent()) {
            return Optional.empty();
        }
        AccountEntity account = optionalAccount.get();

        AccountDetailsResponse accountDetails = apiClient.fetchAccountDetails(cardDetails.getAccountNumber());
        if (accountDetails.isUnknownType()) {
            // log unknown type
            logUnknownCardType(account, accountDetails, cardDetails);
        }

        if (!accountDetails.isCreditCardAccount()) {
            return Optional.empty();
        }

        if (Objects.isNull(accountDetails.getAccountHolder()) || Objects.isNull(cardDetails.getCardHolderName()) ||
                !accountDetails.getAccountHolder().equalsIgnoreCase(cardDetails.getCardHolderName())) {
            return Optional.empty();
        }

        return Optional.of(
                CreditCardAccount.builder(account.getAccountId(), account.getTinkBalance(),
                        accountDetails.getTinkMaxAmount())
                    .setAccountNumber(accountDetails.getAccountId())
                    .setHolderName(new HolderName(accountDetails.getAccountHolder()))
                    .setName(account.getAccountName())
                    .build()
        );
    }

    private void logUnknownCardType(AccountEntity account, AccountDetailsResponse accountDetails,
            CardDetailsResponse cardDetails) {
        LOGGER.infoExtraLong(
                String.format("Account: %s\nAccountDetails: %s\nCardDetails: %s",
                        SerializationUtils.serializeToString(account),
                        SerializationUtils.serializeToString(accountDetails),
                        SerializationUtils.serializeToString(cardDetails)),
                BecConstants.Log.UNKNOWN_CREDITCARD);
    }
}
