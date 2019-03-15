package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordeadk.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordeadk.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

@JsonObject
public class NordeaDkTransactionalAccountFetcher extends NordeaBaseTransactionalAccountFetcher {

    public NordeaDkTransactionalAccountFetcher(NordeaDkApiClient apiClient) {
        super(apiClient);
    }
}
