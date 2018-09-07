package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

/**
 * Types of account using this request
 *
 * cuenta: ["cuentas/BTAT", "A32"],
 * cuentacredito: ["cuentas/BTAT", "A45"],
 * fondseleccion: ["cuentas/BTAT", "J01"],
 * prestamo: ["cuentas/BTAT", "K11"],
 * seguro: ["cuentas/BTAT", "F99"],
 *
 * "cuentas/BTAT"
 * {
 *  identificador: d[n][1],
 *  nivel: "002",
 *  OCURRENCIAS: "15",
 *  INDICADOR_3: "1",
 *  INDICADOR_5: "4",
 *  page: "full"
 * }
 *
 * currently only fetching cuenta
 */
@JsonObject
public class FetchAccountsRequest {
    private String identificador;
    private String nivel;
    private String ocurrencias;
    private String indicador3;
    private String indicador5;
    private String page;

    private FetchAccountsRequest(String accountIdentifier) {
        this.identificador = accountIdentifier;
        this.nivel = BancoPopularConstants.Fetcher.NIVEL;
        this.ocurrencias = BancoPopularConstants.Fetcher.OCURRENCIAS;
        this.indicador3 = BancoPopularConstants.Fetcher.INDICADOR_3;
        this.indicador5 = BancoPopularConstants.Fetcher.INDICADOR_5;
        this.page = BancoPopularConstants.Fetcher.ACCOUNTS_PAGE;

    }

    public static FetchAccountsRequest build(String accountIdentifier) {
        return new FetchAccountsRequest(accountIdentifier);
    }
}
