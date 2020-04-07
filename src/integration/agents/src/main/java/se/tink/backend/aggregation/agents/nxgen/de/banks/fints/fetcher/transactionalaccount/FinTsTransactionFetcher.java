package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction.TransactionClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction.TransactionRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.FinTsTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.SupportedRequestSegments;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HICAZ;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIKAZ;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@AllArgsConstructor
public class FinTsTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    public enum StrategyType {
        CAMT,
        SWIFT
    }

    @AllArgsConstructor
    private static class Strategy {
        private final StrategyType type;
        private final int version;
    }

    private final FinTsDialogContext dialogContext;
    private final TransactionClient transactionClient;
    private final FinTsTransactionMapper mapper;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(
            TransactionalAccount transactionalAccount) {
        Optional<FinTsAccountInformation> maybeFinTsAccount =
                dialogContext.getAccounts().stream()
                        .filter(
                                finTsAccount ->
                                        finTsAccount
                                                .getBasicInfo()
                                                .getAccountNumber()
                                                .equals(transactionalAccount.getAccountNumber()))
                        .findFirst();
        if (!maybeFinTsAccount.isPresent()) {
            throw new IllegalArgumentException(
                    "Could not find FinTsAccountInformation for provided account");
        }

        FinTsAccountInformation finTsAccount = maybeFinTsAccount.get();
        Strategy strategy = pickStrategy(finTsAccount);
        return executeStrategy(strategy, finTsAccount);
    }

    private Strategy pickStrategy(FinTsAccountInformation account) {
        if (account.getBasicInfo().isOperationSupported(SegmentType.HKCAZ)) {
            OptionalInt version =
                    SupportedRequestSegments.getHighestCommonVersion(
                            dialogContext, SegmentType.HKCAZ);
            if (version.isPresent()) {
                return new Strategy(StrategyType.CAMT, version.getAsInt());
            }
        }
        return getFallbackStrategy(account);
    }

    private Strategy getFallbackStrategy(FinTsAccountInformation account) {
        if (!account.getBasicInfo().isOperationSupported(SegmentType.HKKAZ)) {
            throw new IllegalArgumentException(
                    "Could not provide fallback strategy - HKKAZ not supported");
        }
        int version =
                SupportedRequestSegments.getHighestCommonVersionThrowable(
                        dialogContext, SegmentType.HKKAZ);
        return new Strategy(StrategyType.SWIFT, version);
    }

    private List<AggregationTransaction> executeStrategy(
            Strategy strategy, FinTsAccountInformation account) {
        TransactionRequestBuilder requestBuilder =
                TransactionRequestBuilder.getRequestBuilder(strategy.type, strategy.version);

        List<FinTsResponse> transactionResponses =
                transactionClient.getTransactionResponses(requestBuilder, account);

        switch (strategy.type) {
            case CAMT:
                return transactionResponses.stream()
                        .flatMap(x -> x.findSegments(HICAZ.class).stream())
                        .flatMap(hicaz -> hicaz.getCamtFiles().stream())
                        .flatMap(x -> mapper.parseCamt(x).stream())
                        .collect(Collectors.toList());
            case SWIFT:
                return transactionResponses.stream()
                        .flatMap(x -> x.findSegments(HIKAZ.class).stream())
                        .flatMap(hikaz -> Stream.of(hikaz.getBooked(), hikaz.getNotBooked()))
                        .filter(Objects::nonNull)
                        .flatMap(x -> mapper.parseSwift(x).stream())
                        .collect(Collectors.toList());
            default:
                throw new UnsupportedOperationException(
                        "We do not support this fetching strategy type: " + strategy.type);
        }
    }
}
