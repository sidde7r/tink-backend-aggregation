package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
public class DateLimitTransactionPaginatorHelperFactory {

    public TransactionPaginationHelper create(CredentialsRequest request, Date dateLimit) {

        if (request.getUserAvailability().isUserPresent()) {
            log.info("Using DateLimitTransactionPaginationHelper");
            return new DateLimitTransactionPaginationHelper(dateLimit, request);
        }

        log.info("Using CertainDateTransactionPaginationHelper");
        return new CertainDateTransactionPaginationHelper(request);
    }
}
