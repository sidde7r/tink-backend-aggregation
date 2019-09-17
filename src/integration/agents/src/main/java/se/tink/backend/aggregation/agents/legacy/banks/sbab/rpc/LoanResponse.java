package se.tink.backend.aggregation.agents.banks.sbab.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import se.tink.backend.aggregation.agents.banks.sbab.entities.LoanEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanResponse extends ArrayList<LoanEntity> {}
