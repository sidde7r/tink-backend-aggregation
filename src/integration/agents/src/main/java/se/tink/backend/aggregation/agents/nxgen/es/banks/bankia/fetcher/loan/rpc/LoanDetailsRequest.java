package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaRequest;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class LoanDetailsRequest implements BankiaRequest {
    @JsonProperty("clavePaginacionEntrada")
    private String continuationKey = "";

    @JsonProperty("codigoLineaRiesgo")
    private String code;

    @JsonProperty("identificadorExpediente")
    private String loanIdentifier;

    public LoanDetailsRequest(String code) {
        this.code = code;
    }

    public LoanDetailsRequest setLoanIdentifier(String loanIdentifier) {
        this.loanIdentifier = loanIdentifier;
        return this;
    }

    public LoanDetailsRequest setContinuationKey(String continuationKey) {
        this.continuationKey = continuationKey;
        return this;
    }

    @Override
    @JsonIgnore
    public URL getURL() {
        return URL.of(
                BankiaConstants.URL_BASE_OIP
                        + "/api/1.0/operativas/1.0/prestamos/ObtenerDatosGeneralesPrestamoSBP");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LoanDetailsRequest that = (LoanDetailsRequest) o;
        return Objects.equals(continuationKey, that.continuationKey)
                && Objects.equals(code, that.code)
                && Objects.equals(loanIdentifier, that.loanIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(continuationKey, code, loanIdentifier);
    }
}
