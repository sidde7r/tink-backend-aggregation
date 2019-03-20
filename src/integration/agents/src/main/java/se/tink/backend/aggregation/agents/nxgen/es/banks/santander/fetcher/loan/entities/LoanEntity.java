package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.CustomerData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.GeneralInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "prestamo")
public class LoanEntity {

    @JsonProperty("comunes")
    private GeneralInfoEntity generalInfo;

    @JsonProperty("titular")
    private CustomerData customerData;

    @JsonProperty("descSituacionContrato")
    private String contractDescription;

    @JsonProperty("impSaldoDispuesto")
    private AmountEntity balance;

    @JsonProperty("impDisponible")
    private AmountEntity availableAmount;

    @JsonProperty("impDisponibleContravalor")
    private AmountEntity availableAmountCounterValue;

    @JsonProperty("importeSalDisptoContravalor")
    private AmountEntity balanceCounterValue;

    public AmountEntity getBalance() {
        return balance;
    }

    public GeneralInfoEntity getGeneralInfo() {
        return generalInfo;
    }
}
