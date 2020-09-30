package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites.CreditCardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class SparebankenSorCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final SparebankenSorApiClient apiClient;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return Optional.ofNullable(apiClient.fetchCreditCards().getCreditCards())
                .orElseGet(Collections::emptyList).stream()
                .map(
                        creditCardEntity -> {
                            logCreditCardTransactions(creditCardEntity);
                            return creditCardEntity.toTinkCreditCard();
                        })
                .collect(Collectors.toList());
    }

    private void logCreditCardTransactions(CreditCardEntity creditCardEntity) {
        LinkEntity detailsLink =
                creditCardEntity.getLinks().get(SparebankenSorConstants.Link.TRANSACTIONS);

        if (detailsLink == null || Strings.isNullOrEmpty(detailsLink.getHref())) {
            log.warn(
                    SparebankenSorConstants.LogTags.CREDIT_CARD_LOG_TAG.toString()
                            + " no link to credit card details present.");
            return;
        }

        try {
            apiClient.fetchDetails(detailsLink.getHref());
        } catch (HttpResponseException e) {
            log.warn(
                    SparebankenSorConstants.LogTags.CREDIT_CARD_LOG_TAG.toString()
                            + " fetching of credit card details failed",
                    e);
        }
    }
}
