package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.AccountOverviewEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse extends ArrayList<AccountOverviewEntity> {
}
