package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.TransactionsResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements PaginatorResponse {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private TransactionsResponseEntity mobileResponse;

    public TransactionsResponseEntity getMobileResponse() {
        Preconditions.checkNotNull(mobileResponse);
        mobileResponse.validateSession();
        return mobileResponse;
    }

    @Override
    public Collection<Transaction> getTinkTransactions() {
        if (!fetchedInsideAllowedRange()) {
            return Collections.emptyList();
        }

        return getMobileResponse().getMovements().stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(fetchedInsideAllowedRange() && !fetchedLessThanFullBatch());
    }

    private boolean fetchedInsideAllowedRange() {
        String returnCode = getMobileResponse().getReturnCode();

        if (IngConstants.ReturnCodes.OK.equalsIgnoreCase(returnCode)) {
            return true;
        }

        if (!IngConstants.ReturnCodes.NOK.equalsIgnoreCase(returnCode)) {
            // Don't know if there are other codes than ok and nok, logging those here if so.
            logger.warn(
                    String.format(
                            "%s: %s", IngConstants.LogMessage.UNKNOWN_RETURN_CODE, returnCode));
            throw new IllegalStateException(
                    String.format("%s", IngConstants.LogMessage.TRANSACTION_FETCHING_ERROR));
        }

        Optional<String> errorCode = getMobileResponse().getErrorCode();

        if (errorCode.isPresent()) {
            if (IngConstants.ErrorCodes.FETCHED_TRANSACTIONS_OUTSIDE_RANGE_CODE.equalsIgnoreCase(
                    errorCode.get())) {
                return false;
            } else if (IngConstants.ErrorCodes.STARTING_DATE_ENTERED_IS_WRONG.equalsIgnoreCase(
                    errorCode.get())) {
                return false;
            } else if (ErrorCodes.ACCOUNT_NOT_VALID.equalsIgnoreCase(errorCode.get())) {
                return false;
            } else {
                throw new IllegalStateException(
                        String.format(
                                "%s with error code: %s",
                                IngConstants.LogMessage.TRANSACTION_FETCHING_ERROR,
                                errorCode.get()));
            }
        }

        throw new IllegalStateException(
                String.format("%s", IngConstants.LogMessage.TRANSACTION_FETCHING_ERROR));
    }

    private boolean fetchedLessThanFullBatch() {
        return getMobileResponse().getMovements().size()
                < IngConstants.Fetcher.MAX_TRANSACTIONS_IN_BATCH;
    }
}
