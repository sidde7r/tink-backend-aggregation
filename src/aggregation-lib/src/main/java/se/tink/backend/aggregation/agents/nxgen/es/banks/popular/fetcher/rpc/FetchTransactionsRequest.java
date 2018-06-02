package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;

public class FetchTransactionsRequest {
    private String numIntContrato;
    private String cccBanco;
    private String cccSucursal;
    private String cccDigitos;
    private String cccSubcuenta;
    private String cccCuenta;
    private String cccDigitosCtrol;
    private String fechaDesde;
    private String fechaHasta;
    private String tipoMovEcrmvto2 = BancoPopularConstants.Fetcher.TIPO_MOV_ECRMVTO_2;
    private int concepEcrmvto2 = BancoPopularConstants.Fetcher.CONCEP_ECRMVTO_2;

    // following fields depend on account number
    public FetchTransactionsRequest updateCccFields(String accountNumber) {
        accountNumber = extractAccountNumber(accountNumber);
        cccDigitos = accountNumber.substring(0, 2);
        cccSubcuenta = accountNumber.substring(2, 5);
        cccCuenta = accountNumber.substring(5, 10);
        cccDigitosCtrol = accountNumber.substring(accountNumber.length() - 2);

        return this;
    }

    // account number is set to iban, so just use the 12 last characters in the number
    private String extractAccountNumber(String accountNumber) {
        String normalizedAccountNumber = accountNumber.replaceAll(" ", "");
        if (normalizedAccountNumber.length() > 12) {
            normalizedAccountNumber = normalizedAccountNumber.substring(normalizedAccountNumber.length() - 12);
        }

        return normalizedAccountNumber;
    }

    public String getNumIntContrato() {
        return numIntContrato;
    }

    public FetchTransactionsRequest setNumIntContrato(String numIntContrato) {
        this.numIntContrato = numIntContrato;
        return this;
    }

    public String getCccBanco() {
        return cccBanco;
    }

    public FetchTransactionsRequest setCccBanco(String cccBanco) {
        this.cccBanco = cccBanco;
        return this;
    }

    public String getCccSucursal() {
        return cccSucursal;
    }

    public FetchTransactionsRequest setCccSucursal(String cccSucursal) {
        this.cccSucursal = cccSucursal;
        return this;
    }

    public String getCccDigitos() {
        return cccDigitos;
    }

    public FetchTransactionsRequest setCccDigitos(String cccDigitos) {
        this.cccDigitos = cccDigitos;
        return this;
    }

    public String getCccSubcuenta() {
        return cccSubcuenta;
    }

    public FetchTransactionsRequest setCccSubcuenta(String cccSubcuenta) {
        this.cccSubcuenta = cccSubcuenta;
        return this;
    }

    public String getCccCuenta() {
        return cccCuenta;
    }

    public FetchTransactionsRequest setCccCuenta(String cccCuenta) {
        this.cccCuenta = cccCuenta;
        return this;
    }

    public String getCccDigitosCtrol() {
        return cccDigitosCtrol;
    }

    public FetchTransactionsRequest setCccDigitosCtrol(String cccDigitosCtrol) {
        this.cccDigitosCtrol = cccDigitosCtrol;
        return this;
    }

    public String getFechaDesde() {
        return fechaDesde;
    }

    public FetchTransactionsRequest setFechaDesde(String fechaDesde) {
        this.fechaDesde = fechaDesde;
        return this;
    }

    public String getFechaHasta() {
        return fechaHasta;
    }

    public FetchTransactionsRequest setFechaHasta(String fechaHasta) {
        this.fechaHasta = fechaHasta;
        return this;
    }

    public String getTipoMovEcrmvto2() {
        return tipoMovEcrmvto2;
    }

    public int getConcepEcrmvto2() {
        return concepEcrmvto2;
    }
}
