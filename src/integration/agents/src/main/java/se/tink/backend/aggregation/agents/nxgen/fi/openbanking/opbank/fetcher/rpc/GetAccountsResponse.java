package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetAccountsResponse extends ArrayList<AccountEntity> {}
