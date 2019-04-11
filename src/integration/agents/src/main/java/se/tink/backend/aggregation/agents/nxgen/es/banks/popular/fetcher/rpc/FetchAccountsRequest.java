package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

/**
 * Types of account using this request
 *
 * <p>cuenta: ["cuentas/BTAT", "A32"], cuentacredito: ["cuentas/BTAT", "A45"], fondseleccion:
 * ["cuentas/BTAT", "J01"], prestamo: ["cuentas/BTAT", "K11"], seguro: ["cuentas/BTAT", "F99"],
 *
 * <p>"cuentas/BTAT" { identificador: d[n][1], nivel: "002", OCURRENCIAS: "15", INDICADOR_3: "1",
 * INDICADOR_5: "4", page: "full" }
 *
 * <p>currently only fetching cuenta
 */
@JsonObject
public class FetchAccountsRequest {
    @JsonProperty("identificador")
    private String identifier;

    @JsonProperty("nivel")
    private String level;

    @JsonProperty("ocurrencias")
    private String occurrences;

    @JsonProperty("indicador3")
    private String indicator3;

    @JsonProperty("indicador5")
    private String indicator5;

    private String page;

    private FetchAccountsRequest(String accountIdentifier) {
        this.identifier = accountIdentifier;
        this.level = BancoPopularConstants.Fetcher.LEVEL;
        this.occurrences = BancoPopularConstants.Fetcher.OCCURRENCES;
        this.indicator3 = BancoPopularConstants.Fetcher.INDICATOR_3;
        this.indicator5 = BancoPopularConstants.Fetcher.INDICATOR_5;
        this.page = BancoPopularConstants.Fetcher.ACCOUNTS_PAGE;
    }

    public static FetchAccountsRequest build(String accountIdentifier) {
        return new FetchAccountsRequest(accountIdentifier);
    }
}
