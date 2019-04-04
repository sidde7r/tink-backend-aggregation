package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.ArrayList;

@JsonObject
public class GetAccountsResponse extends ArrayList<AccountEntity> {}
