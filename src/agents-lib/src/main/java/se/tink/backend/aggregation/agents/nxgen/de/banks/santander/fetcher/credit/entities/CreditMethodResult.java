package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class CreditMethodResult {

  @JsonProperty("nextPageOffset")
  private NextPageOffset nextPageOffset;

  @JsonProperty("movList")
  private List<MovListItem> movList;

  @JsonProperty("isLastPage")
  private String isLastPage;

  public Collection<? extends Transaction> getTinkTransactions() {
    return movList.stream().map(t -> t.getTinkTransactions()).collect(Collectors.toList());
  }

  public boolean getIsLastPage() {
    return "true".equalsIgnoreCase(isLastPage);
  }
}
