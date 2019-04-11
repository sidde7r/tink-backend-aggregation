package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.entities;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.ArrayList;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonRootName("outputs")
public class OutputsEntity extends ArrayList<ValueEntity> {}
