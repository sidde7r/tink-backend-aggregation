package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class NordeaCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final NordeaDkApiClient bankClient;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        CreditCardsResponse creditCardsResponse = bankClient.fetchCreditCards();
        final List<CreditCardAccount> result = new ArrayList<>();
        creditCardsResponse.getCards().stream()
                .filter(CreditCardEntity::isCreditCard)
                .forEach(
                        card ->
                                result.add(
                                        bankClient
                                                .fetchCreditCardDetails(card.getCardId())
                                                .toTinkAccount()));
        return result;
    }
}
