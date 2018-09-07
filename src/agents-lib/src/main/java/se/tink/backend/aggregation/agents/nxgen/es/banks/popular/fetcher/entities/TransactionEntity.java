package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private int numSecuenMov;
    private Date fecmvtoEcrmvto2211;
    private String conceptoMov;   // description
    private String tipoMov;
    private double importeMov;  // amount
    private String signoImporteMov;
    private Date fecvalorEcrmvto221;
    private String codMoneda; // currency
    private double importeCV;  // amount
    private String codMonedaCV;  // currency
    private int indicador;
    private double saldoCont;  //
    private String signoCont;
    private String indE;
    private int refE;
    private String indDuplicado;
    private int codServicio;
    private String contIdentOperServicio;
    private String indicaRss;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(Amount.inEUR(convertToAmount(importeMov, signoImporteMov)))
                .setDate(fecmvtoEcrmvto2211)
                .setDescription(conceptoMov)
                .build();
    }

    // sign is set in separate field
    private double convertToAmount(double importeMov, String signoImporteMov) {
        if (BancoPopularConstants.Fetcher.AMOUNT_SIGN_INDICATOR_1.equalsIgnoreCase(signoImporteMov)
                || BancoPopularConstants.Fetcher.AMOUNT_SIGN_INDICATOR_2.equals(signoImporteMov)) {
            return -importeMov;
        }
        return importeMov;
    }

    public int getNumSecuenMov() {
        return numSecuenMov;
    }

    public void setNumSecuenMov(int numSecuenMov) {
        this.numSecuenMov = numSecuenMov;
    }

    public Date getFecmvtoEcrmvto2211() {
        return fecmvtoEcrmvto2211;
    }

    public void setFecmvtoEcrmvto2211(Date fecmvtoEcrmvto2211) {
        this.fecmvtoEcrmvto2211 = fecmvtoEcrmvto2211;
    }

    public String getConceptoMov() {
        return conceptoMov;
    }

    public void setConceptoMov(String conceptoMov) {
        this.conceptoMov = conceptoMov;
    }

    public String getTipoMov() {
        return tipoMov;
    }

    public void setTipoMov(String tipoMov) {
        this.tipoMov = tipoMov;
    }

    public double getImporteMov() {
        return importeMov;
    }

    public void setImporteMov(double importeMov) {
        this.importeMov = importeMov;
    }

    public String getSignoImporteMov() {
        return signoImporteMov;
    }

    public void setSignoImporteMov(String signoImporteMov) {
        this.signoImporteMov = signoImporteMov;
    }

    public Date getFecvalorEcrmvto221() {
        return fecvalorEcrmvto221;
    }

    public void setFecvalorEcrmvto221(Date fecvalorEcrmvto221) {
        this.fecvalorEcrmvto221 = fecvalorEcrmvto221;
    }

    public String getCodMoneda() {
        return codMoneda;
    }

    public void setCodMoneda(String codMoneda) {
        this.codMoneda = codMoneda;
    }

    public double getImporteCV() {
        return importeCV;
    }

    public void setImporteCV(double importeCV) {
        this.importeCV = importeCV;
    }

    public String getCodMonedaCV() {
        return codMonedaCV;
    }

    public void setCodMonedaCV(String codMonedaCV) {
        this.codMonedaCV = codMonedaCV;
    }

    public int getIndicador() {
        return indicador;
    }

    public void setIndicador(int indicador) {
        this.indicador = indicador;
    }

    public double getSaldoCont() {
        return saldoCont;
    }

    public void setSaldoCont(double saldoCont) {
        this.saldoCont = saldoCont;
    }

    public String getSignoCont() {
        return signoCont;
    }

    public void setSignoCont(String signoCont) {
        this.signoCont = signoCont;
    }

    public String getIndE() {
        return indE;
    }

    public void setIndE(String indE) {
        this.indE = indE;
    }

    public int getRefE() {
        return refE;
    }

    public void setRefE(int refE) {
        this.refE = refE;
    }

    public String getIndDuplicado() {
        return indDuplicado;
    }

    public void setIndDuplicado(String indDuplicado) {
        this.indDuplicado = indDuplicado;
    }

    public int getCodServicio() {
        return codServicio;
    }

    public void setCodServicio(int codServicio) {
        this.codServicio = codServicio;
    }

    public String getContIdentOperServicio() {
        return contIdentOperServicio;
    }

    public void setContIdentOperServicio(String contIdentOperServicio) {
        this.contIdentOperServicio = contIdentOperServicio;
    }

    public String getIndicaRss() {
        return indicaRss;
    }

    public void setIndicaRss(String indicaRss) {
        this.indicaRss = indicaRss;
    }
}
