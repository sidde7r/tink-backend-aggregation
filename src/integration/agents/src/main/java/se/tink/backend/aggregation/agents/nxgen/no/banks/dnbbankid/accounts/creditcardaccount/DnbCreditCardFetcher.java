package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.creditcardaccount;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbExceptionsHelper;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.creditcardaccount.entities.CardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class DnbCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final DnbApiClient apiClient;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<CreditCardAccount> creditcardAccounts = Lists.newArrayList();

        try {
            List<String> cardIds =
                    CollectionUtils.emptyIfNull(apiClient.listCards().getCreditCards()).stream()
                            .filter(CardEntity::isActive)
                            .map(CardEntity::getCardid)
                            .collect(Collectors.toList());

            for (String cardId : cardIds) {
                creditcardAccounts.add(apiClient.getCard(cardId).toTinkCard(cardId));
            }
            return creditcardAccounts;

        } catch (HttpResponseException e) {
            if (DnbExceptionsHelper.customerDoesNotHaveAccessToResource(e)
                    || DnbExceptionsHelper.noResourceFoundForTheCustomer(e)) {
                return creditcardAccounts;
            }
            throw e;
        }
    }
}
