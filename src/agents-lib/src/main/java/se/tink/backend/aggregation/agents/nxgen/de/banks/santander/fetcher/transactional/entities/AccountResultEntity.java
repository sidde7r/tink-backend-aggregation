package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

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
            .filter(acc -> acc.isValid())
            .map(CurrentEntity::toTransactionalAccount)
            .collect(Collectors.toList()));

    return result;
  }

  public boolean containsCreditCards() {
    return !listCredit.isEmpty();
  }

  public String getLocalContractType() {
    if (!listCurrent.isEmpty()) {
      return listCurrent.get(0).getAccountEntity().getAccountNumber().getLocalContractType();
    }
    return listCredit.get(0).getCards().getLocalContractType();
  }

  public String getLocalContractDetail() {
    if (!listCurrent.isEmpty()) {
      return listCurrent.get(0).getAccountEntity().getAccountNumber().getLocalContractDetail();
    }

    return listCredit.get(0).getCards().getDetailContractLocal();
  }

  public String getCreditPan() {
    return listCredit.get(0).getCards().getPanTarjeta();
  }

  public String getCompanyId() {
    if (!listCurrent.isEmpty()) {
      return listCurrent
          .get(0)
          .getAccountEntity()
          .getSubProductEntity()
          .getProductEntity()
          .getCompanyId();
    }
    return listCredit.get(0).getCards().getCompanyId();
  }
}
