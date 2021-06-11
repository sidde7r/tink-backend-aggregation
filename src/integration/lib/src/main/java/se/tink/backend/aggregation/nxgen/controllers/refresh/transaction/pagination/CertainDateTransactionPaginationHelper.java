package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.credentials.service.CredentialsRequest;

/**
 * @deprecated This class ensures compatibility with old approach to certainDate for account fetch.
 *     Will be replaced by TransactionPaginationHelper.
 */
@Deprecated
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class CertainDateTransactionPaginationHelper extends TransactionPaginationHelper {

    private final CredentialsRequest request;

    @Override
    public Optional<Date> getTransactionDateLimit(Account account) {
        if (request.getAccounts() == null) {
            return Optional.empty();
        }

        return request.getAccounts().stream()
                .filter(a -> account.isUniqueIdentifierEqual(a.getBankId()))
                .map(se.tink.backend.agents.rpc.Account::getCertainDate)
                .filter(Objects::nonNull)
                .findFirst();
    }
}
