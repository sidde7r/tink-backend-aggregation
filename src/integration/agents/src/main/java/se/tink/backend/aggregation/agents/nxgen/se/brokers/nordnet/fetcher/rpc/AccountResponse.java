package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountResponse extends ArrayList<AccountEntity> {}
