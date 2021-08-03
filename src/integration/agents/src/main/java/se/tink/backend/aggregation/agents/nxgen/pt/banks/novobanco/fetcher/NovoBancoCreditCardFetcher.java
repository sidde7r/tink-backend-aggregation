package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard.CardAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard.CardListEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard.ContextCardsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard.GetCardDetailsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.creditcard.GetCreditCardDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class NovoBancoCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final NovoBancoApiClient apiClient;

    public NovoBancoCreditCardFetcher(NovoBancoApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CreditCardAccount> resultAccounts = new ArrayList<>();
        NovoBancoApiClient.CreditCardAggregatedData response = apiClient.getCreditCards();

        Collection<GetCreditCardDetailsResponse> creditCards =
                response.getCreditCardDetailsResponses();
        Collection<AccountDetailsEntity> accounts = response.getAccountsDetails();
        creditCards.forEach(
                creditCard ->
                        getCardListStream(creditCard)
                                .forEach(
                                        cardList -> {
                                            resultAccounts.addAll(
                                                    getMappedAccounts(accounts, cardList));
                                        }));
        return resultAccounts;
    }

    private Collection<CreditCardAccount> getMappedAccounts(
            Collection<AccountDetailsEntity> accounts, CardListEntity cardList) {
        AccountDetailsEntity accountDetails =
                accounts.stream()
                        .filter(acc -> Objects.equals(acc.getId(), cardList.getDoAccount()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find a matching account"));
        return CreditCardAccountMapper.mapToTinkAccounts(cardList, accountDetails);
    }

    private Stream<CardListEntity> getCardListStream(GetCreditCardDetailsResponse creditCard) {
        return Optional.of(creditCard)
                .map(GetCreditCardDetailsResponse::getBody)
                .map(GetCardDetailsBodyEntity::getContextCards)
                .map(ContextCardsEntity::getCardAccounts)
                .map(CardAccountsEntity::getCardList)
                .map(Collection::stream)
                .orElse(Stream.empty());
    }
}
