package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.ContractOverviewEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractsResponse extends ArrayList<ContractOverviewEntity> {
}
