package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

// Types of account using this request
/*
cuenta: ["cuentas/BTAT", "A32"],
cuentacredito: ["cuentas/BTAT", "A45"],
fondseleccion: ["cuentas/BTAT", "J01"],
prestamo: ["cuentas/BTAT", "K11"],
seguro: ["cuentas/BTAT", "F99"],

"cuentas/BTAT"
    {
        identificador: d[n][1],
        nivel: "002",
        OCURRENCIAS: "15",
        INDICADOR_3: "1",
        INDICADOR_5: "4",
        page: "full"
    }

currently only fetching cuenta
*/

import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;

public class FetchAccountsRequest {
    private String identificador;
    private String nivel = BancoPopularConstants.Fetcher.NIVEL;
    private String ocurrencias = BancoPopularConstants.Fetcher.OCURRENCIAS;
    private String indicador3 = BancoPopularConstants.Fetcher.INDICADOR_3;
    private String indicador5 = BancoPopularConstants.Fetcher.INDICADOR_5;
    private String page = BancoPopularConstants.Fetcher.ACCOUNTS_PAGE;


    public String getIdentificador() {
        return identificador;
    }

    public FetchAccountsRequest setIdentificador(String identificador) {
        this.identificador = identificador;
        return this;
    }

    public String getNivel() {
        return nivel;
    }

    public String getOcurrencias() {
        return ocurrencias;
    }

    public String getIndicador3() {
        return indicador3;
    }

    public String getIndicador5() {
        return indicador5;
    }

    public String getPage() {
        return page;
    }
}
