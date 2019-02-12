package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import se.tink.backend.aggregation.agents.abnamro.client.model.PfmContractEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PfmContractResponse extends ArrayList<PfmContractEntity> {

}
