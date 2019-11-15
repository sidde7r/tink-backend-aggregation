package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.InfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "methodResult")
public class LoanDetailsEntity {
    private InfoEntity info;

    @JsonProperty("impCapitalInicial")
    private AmountEntity initialAmount;

    @JsonProperty("cuentaAsociada")
    private ContractEntity contractEntity;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("fechaVencimientoInicial")
    private Date initialEndDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("fechaVencimientoActual")
    private Date currentEndDate;

    @JsonProperty("fechaApertura")
    private String startDate;

    @JsonProperty("descTipoDeInteres")
    private String typeOfIntresetDescription;

    // MESES - monthly
    @JsonProperty("descPerioricidadCuotas")
    private String instalmentsFrequency;

    @JsonProperty("titular")
    private String mainHolder;

    @JsonProperty("descCuentaAsociada")
    private String associateAccountNumber;

    @JsonProperty("tipoDeInteres")
    private String interestl;

    public AmountEntity getInitialAmount() {
        return initialAmount;
    }

    public ContractEntity getContractEntity() {
        return contractEntity;
    }

    public Date getInitialEndDate() {
        return initialEndDate;
    }

    public Date getCurrentEndDate() {
        return currentEndDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getTypeOfIntresetDescription() {
        return typeOfIntresetDescription;
    }

    public String getInstalmentsFrequency() {
        return instalmentsFrequency;
    }

    public String getMainHolder() {
        return Strings.nullToEmpty(mainHolder).trim();
    }

    public String getAssociateAccountNumber() {
        return associateAccountNumber;
    }

    @JsonIgnore
    public String getInterest() {
        return interestl;
    }
}
/*
<descTipoDeInteres>EURIBR.12 M-DIA+4.0</descTipoDeInteres>
 */
