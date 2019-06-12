package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NextLinkEntity {

  private String href;

  public boolean hasNextLink() {return href == null ? false : true; }

  public String getsNextLink() {return href; }
}
