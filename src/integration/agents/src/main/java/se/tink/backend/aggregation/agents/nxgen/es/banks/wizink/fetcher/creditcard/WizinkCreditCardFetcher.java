package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities.CardDetail;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc.CardDetailResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@Slf4j
public class WizinkCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private WizinkApiClient wizinkApiClient;
    private WizinkStorage wizinkStorage;

    public WizinkCreditCardFetcher(WizinkApiClient wizinkApiClient, WizinkStorage wizinkStorage) {
        this.wizinkApiClient = wizinkApiClient;
        this.wizinkStorage = wizinkStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CardEntity> cards = wizinkStorage.getCreditCardList();

        return cards.stream()
                .map(cardEntity -> wizinkApiClient.fetchCreditCardDetails(cardEntity))
                .map(CardDetailResponse::getCardDetail)
                .map(CardDetail::getCardDetailEntity)
                .map(
                        cardDetailEntity ->
                                cardDetailEntity.toTinkAccount(wizinkStorage.getXTokenUser()))
                .collect(Collectors.toList());
    }
}
