package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PretsEntity {
    @JsonProperty("problemeTechnique")
    private boolean technicalProblem;
    @JsonProperty("offresCreditImmo")
    private List<CreditImmoOffersEntity> creditImmoOffers;
    @JsonProperty("offresCreditConso")
    private List<CreditConsoOffersEntity> creditConsoOffers;
}
