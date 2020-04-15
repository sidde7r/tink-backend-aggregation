package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction.TransactionClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount.detail.CamtStrategy;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount.detail.FinTsTransactionFetchingStrategy;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount.detail.SwiftStrategy;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.FinTsTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.SupportedRequestSegments;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@AllArgsConstructor
public class FinTsTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

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
        FinTsTransactionFetchingStrategy strategy = pickStrategy(finTsAccount);
        return strategy.execute(finTsAccount);
    }

    private FinTsTransactionFetchingStrategy pickStrategy(FinTsAccountInformation account) {
        if (account.getBasicInfo().isOperationSupported(SegmentType.HKCAZ)) {
            OptionalInt version =
                    SupportedRequestSegments.getHighestCommonVersion(
                            dialogContext, SegmentType.HKCAZ);
            if (version.isPresent()) {
                return new CamtStrategy(transactionClient, mapper, version.getAsInt());
            }
        }
        return getFallbackStrategy(account);
    }

    private FinTsTransactionFetchingStrategy getFallbackStrategy(FinTsAccountInformation account) {
        if (!account.getBasicInfo().isOperationSupported(SegmentType.HKKAZ)) {
            throw new IllegalArgumentException(
                    "Could not provide fallback strategy - HKKAZ not supported");
        }
        int version =
                SupportedRequestSegments.getHighestCommonVersionThrowable(
                        dialogContext, SegmentType.HKKAZ);
        return new SwiftStrategy(transactionClient, mapper, version);
    }
}
