package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MethodResult {

  @JsonProperty("lastSettlement")
  private String lastSettlement;

  @JsonProperty("lastMovementData")
  private String lastMovementData;

  @JsonProperty("cardHolderName")
  private String cardHolderName;

  @JsonProperty("paymentMode")
  private String paymentMode;

  @JsonProperty("autorisedSaldo")
  private AutorisedSaldo autorisedSaldo;

  @JsonProperty("saldo")
  private Saldo saldo;

  @JsonProperty("productName")
  private String productName;

  @JsonProperty("availableAmount")
  private AvailableAmount availableAmount;

  @JsonProperty("referenceAccount")
  private ReferenceAccount referenceAccount;

  @JsonProperty("mainCardPan")
  private String mainCardPan;

  @JsonProperty("limit")
  private Limit limit;

  @JsonProperty("localAccount")
  private LocalAccount localAccount;

  @JsonProperty("interventionType")
  private String interventionType;

  @JsonProperty("dueNextDirectDebit")
  private String dueNextDirectDebit;

  @JsonProperty("pan")
  private Object pan;

  @JsonProperty("nextCardSettelmentDate")
  private String nextCardSettelmentDate;

  public String getLastSettlement() {
    return lastSettlement;
  }

  public String getLastMovementData() {
    return lastMovementData;
  }

  public String getCardHolderName() {
    return cardHolderName;
  }

  public String getPaymentMode() {
    return paymentMode;
  }

  public AutorisedSaldo getAutorisedSaldo() {
    return autorisedSaldo;
  }

  public Saldo getSaldo() {
    return saldo;
  }

  public String getProductName() {
    return productName;
  }

  public AvailableAmount getAvailableAmount() {
    return availableAmount;
  }

  public ReferenceAccount getReferenceAccount() {
    return referenceAccount;
  }

  public String getMainCardPan() {
    return mainCardPan;
  }

  public Limit getLimit() {
    return limit;
  }

  public LocalAccount getLocalAccount() {
    return localAccount;
  }

  public String getInterventionType() {
    return interventionType;
  }

  public String getDueNextDirectDebit() {
    return dueNextDirectDebit;
  }

  public Object getPan() {
    return pan;
  }

  public String getNextCardSettelmentDate() {
    return nextCardSettelmentDate;
  }
}
