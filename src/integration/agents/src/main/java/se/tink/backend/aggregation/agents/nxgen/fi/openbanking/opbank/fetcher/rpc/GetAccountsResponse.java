package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.ArrayList;

@JsonObject
public class GetAccountsResponse extends ArrayList<AccountEntity> {

}
