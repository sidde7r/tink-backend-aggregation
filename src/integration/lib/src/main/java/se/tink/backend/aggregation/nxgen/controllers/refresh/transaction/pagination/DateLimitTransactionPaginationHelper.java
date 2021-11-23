package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import java.util.Date;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DateLimitTransactionPaginationHelper extends TransactionPaginationHelper {

    private final Date dateLimit;
    private final CredentialsRequest request;

    @Override
    public Optional<Date> getTransactionDateLimit(Account account) {
        if (request.getAccounts() == null) {
            return Optional.empty();
        }

        return Optional.of(dateLimit);
    }
}
