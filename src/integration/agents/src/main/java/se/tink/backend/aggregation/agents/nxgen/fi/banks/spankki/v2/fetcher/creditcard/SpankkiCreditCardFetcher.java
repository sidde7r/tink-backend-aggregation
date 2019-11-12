package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SpankkiCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final SpankkiApiClient apiClient;

    public SpankkiCreditCardFetcher(SpankkiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        final Collection<CreditCardAccount> creditCardAccountList =
                apiClient.fetchCards().getCards().stream()
                        .filter(CardsEntity::isNotDebit)
                        .map(this::fetchCardDetails)
                        .collect(Collectors.toList()); // currently only logs credit cards

        return Collections.emptyList();
    }

    private CreditCardAccount fetchCardDetails(CardsEntity cardsEntity) {
        // Fetching transactions has only been implemented here temporarily to log credit card
        // transactions. A transaction fetcher will be implemented once we have logs.
        fetchCardTransactions(cardsEntity);
        apiClient
                .fetchCardDetails(cardsEntity.getContractNr(), cardsEntity.getProductCode())
                .getCardInfo()
                .toTinkCard();

        return null;
    }

    private void fetchCardTransactions(CardsEntity cardsEntity) {
        final Calendar fromCalendarDate = calculateFromDate();
        final String fromDate = formatDate(fromCalendarDate.getTime());
        final Calendar toCalendarDate = calculateToDate(fromCalendarDate);
        final String toDate = formatDate(toCalendarDate.getTime());
        apiClient
                .fetchCardTransactions(cardsEntity.getContractNr(), fromDate, toDate)
                .toTinkCardTransactions(); // currently only logs credit card transactions.
    }

    // Mock transaction date pagination. This will be removed once we have logs of credit card
    // transactions
    private static Calendar calculateFromDate() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -3);
        cal.set(Calendar.DATE, 1);

        return cal;
    }

    private static Calendar calculateToDate(Calendar cal) {
        cal.add(Calendar.MONTH, 3);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));

        return cal;
    }

    private static String formatDate(Date date) {
        final DateTimeFormatter dateFormat =
                DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH);
        final LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return dateFormat.format(localDate);
    }
}
