package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class Cards {

  @JsonProperty("accountAlias")
  private String accountAlias;

  @JsonProperty("fecha")
  private String date;

  @JsonProperty("cestaNegocio")
  private String businessBasket;

  @JsonProperty("subProducto")
  private SubProduct subProducto;

  @JsonProperty("listContenidos")
  private List<ListContentItem> listContenidos;

  @JsonProperty("accountType")
  private String accountType;

  @JsonProperty("accountNumberSort")
  private String accountNumberSort;

  @JsonProperty("cestaPresentacion")
  private String presentation;

  @JsonProperty("panTarjeta")
  private String cardPan;

  @JsonProperty("accountNumber")
  private AccountNumber accountNumber;

  @JsonProperty("availableBalance")
  private AvailableBalance availableBalance;

  public boolean isValid() {
    return true;
  }

  private String getUniqueIdentifier() {
    return accountNumberSort;
  }

  private Amount getAvailableCredit() {
    return availableBalance.toTinkAmount();
  }


  public String getCardPan() {
    return cardPan;
  }

  public String getLocalContractType() {
    return accountNumber.getLocalContractType();
  }

  public String getDetailContractLocal() {
    return accountNumber.getDetailContractLocal();
  }

  public String getCompanyId() {
    return subProducto.gettIPODEPRODUCTO().getCompany();
  }
}
