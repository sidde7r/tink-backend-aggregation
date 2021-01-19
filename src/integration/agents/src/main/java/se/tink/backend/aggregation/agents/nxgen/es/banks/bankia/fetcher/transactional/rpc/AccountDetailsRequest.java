package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaRequest;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
@EqualsAndHashCode
public class AccountDetailsRequest implements BankiaRequest {

    @JsonProperty("identificadorCuenta")
    private final String iban;

    public AccountDetailsRequest(String iban) {
        this.iban = iban;
    }

    public String getIban() {
        return iban;
    }

    @JsonIgnore
    @Override
    public URL getURL() {
        return URL.of(
                BankiaConstants.URL_BASE_OIP
                        + "/api/1.0/servicios/cuentas.obtenerDetalleCuenta/2.0/cuentas/obtenerDetalleCuenta");
    }
}
