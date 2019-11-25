package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardDetailsEntity {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("NomeNoCartao")
    private String nameOnCard;

    @JsonProperty("Marca")
    private String brand;

    @JsonProperty("Tipo")
    private String type;

    @JsonProperty("NumeroCartao")
    private String cardNumber;

    @JsonProperty("IdEstado")
    private int cardStateId;

    @JsonProperty("DataValidade")
    private String expirationDate;

    @JsonProperty("LimiteCredito")
    private BigDecimal creditLimit;

    @JsonProperty("SaldoUtilizado")
    private BigDecimal usedBalance;

    @JsonProperty("SaldoDisponivel")
    private BigDecimal availableBalance;

    @JsonProperty("CodigoProduto")
    private String productCode;

    @JsonProperty("Estado")
    private String state;

    //    @JsonProperty("Url")
    //    private String imageUrl;

    @JsonProperty("Validade")
    private String expiration;

    public String getId() {
        return id;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public String getBrand() {
        return brand;
    }

    public String getType() {
        return type;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public int getCardStateId() {
        return cardStateId;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getUsedBalance() {
        return usedBalance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getState() {
        return state;
    }

    public String getExpiration() {
        return expiration;
    }
}
