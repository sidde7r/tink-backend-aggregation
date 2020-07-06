package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity.AccountSummaryEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListAccountsResponse extends ArrayList<AccountSummaryEntity> {}
