package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities.CardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class DnbCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private DnbApiClient apiClient;

    public DnbCreditCardFetcher(DnbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CreditCardAccount> creditcardAccounts = Lists.newArrayList();

        List<String> cardIds = apiClient.listCards().getCreditCards().stream()
                .filter(CardEntity::isActive)
                .map(CardEntity::getCardid)
                .collect(Collectors.toList());

        for (String cardId : cardIds) {
            creditcardAccounts.add(apiClient.getCard(cardId).toTinkCard());
        }

        return creditcardAccounts;
    }
}
