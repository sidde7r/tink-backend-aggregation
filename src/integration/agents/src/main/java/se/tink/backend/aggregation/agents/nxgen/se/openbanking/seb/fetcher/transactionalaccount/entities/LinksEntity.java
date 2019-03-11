package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
  private LinksDetailsEntity next;

  public boolean hasMore() {
    return next != null && next.hasMore();
  }
}
