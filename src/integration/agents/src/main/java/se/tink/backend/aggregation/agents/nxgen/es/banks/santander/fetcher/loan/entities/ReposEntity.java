package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "repos")
public class ReposEntity {

    @JsonProperty("numSecRetrocesion")
    private String numberOfRetrocession;

    @JsonProperty("impRepos")
    private AmountEntity amount;
}
