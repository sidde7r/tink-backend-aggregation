package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.apiclient.AmexApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.converter.AmexTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIdStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.storage.HmacAccountIds;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.HmacToken;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@RequiredArgsConstructor
public class AmexTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private final AmexApiClient amexApiClient;
    private final HmacAccountIdStorage hmacAccountIdStorage;
    private final AmexTransactionalAccountConverter amexTransactionalAccountConverter;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        final HmacToken hmacToken = getHmacTokenForAccountId(account.getAccountNumber());
        final List<TransactionsResponseDto> transactionsResponseList =
                amexApiClient.fetchTransactions(hmacToken);

        return amexTransactionalAccountConverter.convertResponseToAggregationTransactions(
                transactionsResponseList);
    }

    private HmacToken getHmacTokenForAccountId(String accountId) {
        final HmacAccountIds hmacAccountIds =
                hmacAccountIdStorage
                        .get()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No HmacAccountId found in the storage."));

        return Optional.ofNullable(hmacAccountIds.getAccountIdToHmacToken().get(accountId))
                .orElseThrow(
                        () -> new IllegalArgumentException("Hmac token not found for accountId."));
    }
}
