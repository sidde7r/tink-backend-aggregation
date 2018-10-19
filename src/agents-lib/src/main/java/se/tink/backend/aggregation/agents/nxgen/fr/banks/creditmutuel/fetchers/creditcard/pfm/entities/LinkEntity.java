package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.entities;

import com.fasterxml.jackson.annotation.JsonRootName;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonRootName("link")
public class LinkEntity {
   private String method;
   private String type;
   private String url;
}
