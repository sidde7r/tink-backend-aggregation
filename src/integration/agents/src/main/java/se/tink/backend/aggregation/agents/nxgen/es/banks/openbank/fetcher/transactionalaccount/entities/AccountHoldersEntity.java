package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.entities;

import io.vavr.collection.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc.AccountHolderResponse;

@Getter
@AllArgsConstructor
public class AccountHoldersEntity {

    private final String contractNumber;
    private final List<AccountHolderResponse> holders;
}
