package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanResponse extends ArrayList<LoanEntity> {}
