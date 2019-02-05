package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class Cards {

  @JsonProperty("accountAlias")
  private String accountAlias;

  @JsonProperty("fecha")
  private String date;

  @JsonProperty("cestaNegocio")
  private String businessBasket;

  @JsonProperty("subProducto")
  private SubProduct subProduct;

  @JsonProperty("listContenidos")
  private List<ListContentItem> listContentItem;

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
    return subProduct.getKindOfProduct().getCompany();
  }
}
