package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities;

import java.util.HashMap;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidationUnitsEntity extends HashMap<String, List<ValidationUnit>> {}
