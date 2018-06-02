package se.tink.libraries.abnamro.client.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import se.tink.libraries.abnamro.client.model.PfmContractEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PfmContractResponse extends ArrayList<PfmContractEntity> {

}
