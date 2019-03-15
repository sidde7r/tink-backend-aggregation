package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanOverviewEntity {
    private String alias;
    private String nombreFlujoPorDefecto;
    private String nombreFlujoDatos;
    private String aliasUrl;
    private String identificador;
    private boolean saldoInformado;
    @JsonProperty("saldoDisponible")
    private double availableBalance; // current balance for loan?
    @JsonProperty("saldoInicial")
    private int initialBalance;
    private String divisa;
    private String codigoFamilia;
    private String nivelOperatividad;

    public String getAlias() {
        return alias;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public int getInitialBalance() {
        return initialBalance;
    }
}
