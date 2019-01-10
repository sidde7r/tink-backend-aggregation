package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.core.Amount;

@JsonObject
public class Cards {

  @JsonProperty("accountAlias")
  private String accountAlias;

  @JsonProperty("fecha")
  private String fecha;

  @JsonProperty("cestaNegocio")
  private String cestaNegocio;

  @JsonProperty("subProducto")
  private SubProducto subProducto;

  @JsonProperty("listContenidos")
  private List<ListContenidosItem> listContenidos;

  @JsonProperty("accountType")
  private String accountType;

  @JsonProperty("accountNumberSort")
  private String accountNumberSort;

  @JsonProperty("cestaPresentacion")
  private String cestaPresentacion;

  @JsonProperty("panTarjeta")
  private String panTarjeta;

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


  public String getPanTarjeta() {
    return panTarjeta;
  }

  public String getLocalContractType() {
    return accountNumber.getLocalContractType();
  }

  public String getDetailContractLocal() {
    return accountNumber.getDetailContractLocal();
  }

  public String getCompanyId() {
    return subProducto.gettIPODEPRODUCTO().geteMPRESA();
  }
}
