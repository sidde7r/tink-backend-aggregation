package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksDetailsEntity {
  private String href;

  public boolean hasMore() {
    return !Strings.isNullOrEmpty(href)
        && StringUtils.containsIgnoreCase(href, SebConstants.QueryKeys.TRANSACTION_SEQUENCE_NUMBER);
  }
}
