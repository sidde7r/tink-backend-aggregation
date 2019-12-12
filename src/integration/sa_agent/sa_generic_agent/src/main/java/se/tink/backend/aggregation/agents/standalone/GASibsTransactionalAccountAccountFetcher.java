package se.tink.backend.aggregation.agents.standalone;

import java.util.Collection;
import se.tink.backend.aggregation.agents.standalone.grpc.CheckingService;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class GASibsTransactionalAccountAccountFetcher
        implements AccountFetcher<TransactionalAccount> {

    private final CheckingService checkingService;

    public GASibsTransactionalAccountAccountFetcher(CheckingService checkingService) {
        this.checkingService = checkingService;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return checkingService.fetchCheckingAccounts();
    }
}
