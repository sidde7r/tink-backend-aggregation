package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher;

import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

/**
 * In order to construct a complete Tink transaction, data needs to be combined from a Transaction
 * and an Account.
 *
 * @param <ResponseType> Ukob Transaction entity
 * @param <AccountType> Tink account that the transaction belongs to
 * @see <a
 *     href="https://openbanking.atlassian.net/wiki/spaces/DZ/pages/126485500/Transactions+v2.0.0">
 *     Ukob Transaction documentation</a>
 */
public interface TransactionConverter<ResponseType, AccountType extends Account> {

    TransactionKeyPaginatorResponse<String> toPaginatorResponse(
            ResponseType response, AccountType account);
}
