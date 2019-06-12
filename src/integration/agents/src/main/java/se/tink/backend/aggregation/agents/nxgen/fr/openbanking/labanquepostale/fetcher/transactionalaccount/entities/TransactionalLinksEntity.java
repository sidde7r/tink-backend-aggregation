package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionalLinksEntity {
  private NextLinkEntity next;

  public boolean hasNextLink(){ return next == null ? false : next.hasNextLink(); }

  public String getNextLink() {return next.getsNextLink(); }
}
