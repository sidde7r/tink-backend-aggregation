package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard;

import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants.BookingStatusParameter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkAgentsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.entities.CardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SamlinkCardTransactionFetcher implements TransactionFetcher<CreditCardAccount> {

    private final SamlinkApiClient apiClient;
    private final SamlinkAgentsConfiguration configuration;
    private final LocalDateTimeSource localDateTimeSource;

    public SamlinkCardTransactionFetcher(
            final SamlinkApiClient apiClient,
            final SamlinkAgentsConfiguration configuration,
            LocalDateTimeSource localDateTimeSource) {
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.localDateTimeSource = localDateTimeSource;
    }

    public List<AggregationTransaction> fetchTransactionsFor(final CreditCardAccount account) {
        LocalDate earliestPossible =
                localDateTimeSource.now(ZoneId.of("CET")).toLocalDate().minusMonths(4);
        CardTransactionsResponse cardTransactionsResponse =
                apiClient.fetchCardAccountTransactions(
                        new URL(configuration.getBaseUrl().concat(Urls.CARD_TRANSACTIONS))
                                .parameter(PathVariables.ACCOUNT_ID, account.getApiIdentifier())
                                .queryParam(QueryKeys.BOOKING_STATUS, BookingStatusParameter.BOTH)
                                .queryParam(HeaderKeys.DATE_FROM, earliestPossible.toString())
                                .toString());
        return map(cardTransactionsResponse);
    }

    private List<AggregationTransaction> map(CardTransactionsResponse cardTransactionsResponse) {
        List<AggregationTransaction> result = new ArrayList<>();

        if (cardTransactionsResponse != null
                && cardTransactionsResponse.getCardTransactions() != null) {
            result.addAll(
                    cardTransactionsResponse.getCardTransactions().getBooked().stream()
                            .map(this::mapBooked)
                            .collect(Collectors.toList()));
            result.addAll(
                    cardTransactionsResponse.getCardTransactions().getPending().stream()
                            .map(this::mapPending)
                            .collect(Collectors.toList()));
        }

        return result;
    }

    private AggregationTransaction mapBooked(CardTransactionEntity transaction) {
        return Transaction.builder()
                .setPending(false)
                .setDescription(transaction.getTransactionDetails())
                .setDate(transaction.getBookingDate())
                .setAmount(transaction.getTransactionAmount().toAmount())
                .build();
    }

    private AggregationTransaction mapPending(CardTransactionEntity transaction) {
        Builder builder =
                Transaction.builder()
                        .setPending(true)
                        .setDescription(transaction.getTransactionDetails())
                        .setDate(
                                Optional.ofNullable(transaction.getBookingDate())
                                        .orElse(transaction.getTransactionDate()))
                        .setAmount(transaction.getTransactionAmount().toAmount());
        setExternalId(builder, transaction);
        return builder.build();
    }

    private void setExternalId(Builder builder, CardTransactionEntity transaction) {
        if (!Strings.isNullOrEmpty(transaction.getCardTransactionId())) {
            builder.addExternalSystemIds(
                    TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                    transaction.getCardTransactionId());
        }
    }
}
