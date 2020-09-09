package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse extends ArrayList<AccountEntity> {}
