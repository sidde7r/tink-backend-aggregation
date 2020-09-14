package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.date.DateUtils;

@Slf4j
public class TransactionPaginationHelper {
    private static final int SAFETY_THRESHOLD_NUMBER_OF_DAYS = 10;
    private static final int SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS = 10;

    private final CredentialsRequest request;

    public TransactionPaginationHelper(CredentialsRequest request) {
        this.request = request;
    }

    public boolean isContentWithRefresh(
            Account account, List<AggregationTransaction> transactions) {
        if (transactions.size() == 0) {
            log.info("Returning false because transactions list size is 0.");
            return false;
        }

        log.info("Number of transactions: {}", transactions.size());

        if (request.getAccounts() == null || request.getCredentials().getUpdated() == null) {
            log.info("Returning false because account is null or credencial updater is null.");
            return false;
        }

        final Optional<Date> certainDate = getContentWithRefreshDate(account);

        if (!certainDate.isPresent()) {
            log.info("Returning false because certain date is not present.");
            return false;
        }

        log.info("Certain date for this account: {}", certainDate.get());

        // Reached certain date and check next SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS transactions
        // to not be after the previous one.

        AggregationTransaction lastTransaction = null;
        int transactionsBeforeCertainDate = 0;

        for (AggregationTransaction t : transactions) {

            if (lastTransaction == null) {

                if (t.getDate().before(certainDate.get())) {
                    lastTransaction = t;
                }
                continue;

            } else {

                // Certain date reached, check transaction is before last one.

                if (t.getDate().after(certainDate.get())) {

                    // If after, there is a gap in the paging. Start over again and
                    // find next transaction that is before certain date and do this again.

                    lastTransaction = null;
                    transactionsBeforeCertainDate = 0;

                } else {
                    transactionsBeforeCertainDate++;
                }
            }

            log.info("Transactions before certain date: {}", transactionsBeforeCertainDate);

            int overlappingTransactionDays =
                    Math.abs(DateUtils.getNumberOfDaysBetween(t.getDate(), certainDate.get()));

            log.info("Overlaping transactions days: {}", overlappingTransactionDays);

            if (transactionsBeforeCertainDate >= SAFETY_THRESHOLD_NUMBER_OF_OVERLAPS
                    && overlappingTransactionDays >= SAFETY_THRESHOLD_NUMBER_OF_DAYS) {
                log.info(
                        "Returning true because transactionsBeforeCertainDate is grater then safe treshold AND overlapingTransactionDays is greater then safe number of days treshold.");
                return true;
            }
        }

        return false;
    }

    /** Returns the certain date for this account (that is from when we know we have all data) */
    private Optional<Date> getContentWithRefreshDate(final Account account) {
        if (request.getAccounts() == null || request.getCredentials().getUpdated() == null) {
            return Optional.empty();
        }

        return request.getAccounts().stream()
                .filter(
                        a ->
                                account.isUniqueIdentifierEqual(a.getBankId())
                                        && a.getCertainDate() != null)
                .map(se.tink.backend.agents.rpc.Account::getCertainDate)
                .findFirst();
    }
}
