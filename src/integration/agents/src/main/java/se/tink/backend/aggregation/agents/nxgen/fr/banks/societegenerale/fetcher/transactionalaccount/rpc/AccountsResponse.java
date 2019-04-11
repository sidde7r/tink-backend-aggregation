package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities.AccountsData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.rpc.GenericResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse extends GenericResponse<AccountsData> {}
