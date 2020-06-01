package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountListResponse extends ArrayList<AccountEntity> {}
