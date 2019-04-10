package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.fetcher.transactionalaccount.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetAccountsResponse extends ArrayList<AccountEntity> {}
