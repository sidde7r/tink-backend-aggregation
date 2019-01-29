package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountResultEntity {

  private List<CurrentEntity> listCurrent;

  @JsonProperty("listTarjetas")
  private List<ListTarjetasItem> listCredit;

  public List<TransactionalAccount> toTransactionalAccount() {
    List<TransactionalAccount> result = new ArrayList<>();

    result.addAll(
        listCurrent
            .stream()
            .filter(acc -> acc.isTransactionalAccount() && acc.isValid())
            .map(CurrentEntity::toTransactionalAccount)
            .collect(Collectors.toList()));

    return result;
  }

  public boolean containsCreditCards() {
    return !listCredit.isEmpty();
  }

  public List<ListTarjetasItem> getListCredit() {
    return listCredit;
  }
}
