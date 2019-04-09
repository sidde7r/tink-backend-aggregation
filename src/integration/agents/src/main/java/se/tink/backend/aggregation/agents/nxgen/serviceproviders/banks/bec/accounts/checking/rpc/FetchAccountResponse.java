package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountResponse extends ArrayList<AccountEntity> {}
