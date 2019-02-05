package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BancoPopularLoginContract {
    @JsonProperty("nItnCont")
    private long contractNumber;
    @JsonProperty("nOrdIntc")
    private int contractOrder;
    @JsonProperty("cuenta")
    private int account;
    @JsonProperty("oficina")
    private int office;
    private int foIntabr;
    @JsonProperty("formaIntervJurContrato")
    private String intervFormJurContract;
    @JsonProperty("nomTitContrato")
    private String contractHolderName;
    private int cCuadNorm;
    private int cOnline;
    private int codResOper;
    @JsonProperty("codigo")
    private int code;
    @JsonProperty("banco")
    private int bank;
    @JsonProperty("desAlias")
    private String aliases;
    @JsonProperty("estadoMigracion")
    private int establishMigration;

    public long getContractNumber() {
        return contractNumber;
    }

    public int getContractOrder() {
        return contractOrder;
    }

    public int getAccount() {
        return account;
    }

    public int getOffice() {
        return office;
    }

    public int getFoIntabr() {
        return foIntabr;
    }

    public String getIntervFormJurContract() {
        return intervFormJurContract;
    }

    public String getContractHolderName() {
        return contractHolderName;
    }

    public int getcCuadNorm() {
        return cCuadNorm;
    }

    public int getcOnline() {
        return cOnline;
    }

    public int getCodResOper() {
        return codResOper;
    }

    public int getCode() {
        return code;
    }

    public int getBank() {
        return bank;
    }

    public String getAliases() {
        return aliases;
    }

    public int getEstablishMigration() {
        return establishMigration;
    }
}
