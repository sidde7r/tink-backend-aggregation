package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.entities.AccountInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoResponse extends ArrayList<AccountInfoEntity> {}
