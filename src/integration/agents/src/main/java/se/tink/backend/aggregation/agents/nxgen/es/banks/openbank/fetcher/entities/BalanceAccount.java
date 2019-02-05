package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAccount {
    @JsonProperty("fechaAperturaContrato")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date contractOpenedDate;

    @JsonProperty("importeLimite")
    private AmountEntity limitAmount;

    @JsonProperty("descLimiteDescubierto")
    private String discoveredLimitDescription;

    @JsonProperty("importePendiente")
    private AmountEntity pendingAmount;

    @JsonProperty("contrato")
    private ContractEntity contract;

    @JsonProperty("descProducto")
    private String productDescription;

    @JsonProperty("importeAutorizado")
    private AmountEntity authorizedAmount;

    @JsonProperty("importeSaldo")
    private AmountEntity balanceAmount;

    @JsonProperty("retCodigo")
    private String returnCode;

    @JsonProperty("importeRetenido")
    private AmountEntity retainedAmount;

    @JsonProperty("subtipo")
    private ContractSubtypeEntity subtype;

    @JsonProperty("importeDescubierto")
    private AmountEntity discoveredAmount;

    @JsonProperty("importeDispuesto")
    private AmountEntity readyAmount;

    public Date getContractOpenedDate() {
        return contractOpenedDate;
    }

    public AmountEntity getLimitAmount() {
        return limitAmount;
    }

    public String getDiscoveredLimitDescription() {
        return discoveredLimitDescription;
    }

    public AmountEntity getPendingAmount() {
        return pendingAmount;
    }

    public ContractEntity getContract() {
        return contract;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public AmountEntity getAuthorizedAmount() {
        return authorizedAmount;
    }

    public AmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public AmountEntity getRetainedAmount() {
        return retainedAmount;
    }

    public ContractSubtypeEntity getSubtype() {
        return subtype;
    }

    public AmountEntity getDiscoveredAmount() {
        return discoveredAmount;
    }

    public AmountEntity getReadyAmount() {
        return readyAmount;
    }
}
