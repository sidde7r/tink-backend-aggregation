package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardListEntity {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Desc")
    private String desc;

    @JsonProperty("Cartoes")
    private List<CardDetailsEntity> cardsDetails;

    @JsonProperty("LimiteCredito")
    private BigDecimal creditLimit;

    @JsonProperty("CreditoDisponivel")
    private BigDecimal availableCredit;

    @JsonProperty("SaldoUtilizado")
    private BigDecimal usedBalance;

    @JsonProperty("IdFormaPagamento")
    private String paymentMethodId;

    @JsonProperty("FormaPagamento")
    private String paymentMethod;

    @JsonProperty("NumeroConta")
    private String accountNumber;

    @JsonProperty("ContaDO")
    private String doAccount;

    @JsonProperty("IdModalidadeExtrato")
    private String extractTypeId;

    @JsonProperty("ModalidadeExtrato")
    private String extractType;

    @JsonProperty("PercentagemPagamento")
    private BigDecimal percentagePay;

    @JsonProperty("Moeda")
    private String currency;

    public String getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public List<CardDetailsEntity> getCardsDetails() {
        return cardsDetails;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getAvailableCredit() {
        return availableCredit;
    }

    public BigDecimal getUsedBalance() {
        return usedBalance;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getDoAccount() {
        return doAccount;
    }

    public String getExtractTypeId() {
        return extractTypeId;
    }

    public String getExtractType() {
        return extractType;
    }

    public BigDecimal getPercentagePay() {
        return percentagePay;
    }

    public String getCurrency() {
        return currency;
    }
}
