package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.CategoryEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.EarningCategoryEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CardTransactionEntity {
    @JsonProperty("situacionDelMovimiento")
    private String situacionDelMovimiento;

    @JsonProperty("descFormaPago")
    private String descFormaPago;

    @JsonProperty("situacionContratoDESSITUA")
    private String situacionContratoDESSITUA;

    @JsonProperty("numSecDelMovto")
    private int numSecDelMovto;

    @JsonProperty("horaMovimiento")
    private String horaMovimiento;

    @JsonProperty("estadoPeticion")
    private String estadoPeticion;

    @JsonProperty("impOperacion")
    private AmountEntity impOperacion;

    @JsonProperty("redondeo")
    private AmountEntity redondeo;

    @JsonProperty("fechaLiquidacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fechaLiquidacion;

    @JsonProperty("codOperacionBancaria")
    private String codOperacionBancaria;

    @JsonProperty("txtCajero")
    private String description;

    @JsonProperty("descTipoSaldo")
    private String descTipoSaldo;

    @JsonProperty("fechaOperacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fechaOperacion;

    @JsonProperty("impMovimiento")
    private AmountEntity transactionAmount;

    @JsonProperty("codigoOperacionBasica")
    private String codigoOperacionBasica;

    @JsonProperty("indNfc")
    private String indNfc;

    @JsonProperty("categorias")
    private List<CategoryEntity> categories;

    @JsonProperty("indPagoFacil")
    private String indPagoFacil;

    @JsonProperty("numeroOperacionDGO")
    private int numeroOperacionDGO;

    @JsonProperty("categoriaGanadora")
    private EarningCategoryEntity categoriaGanadora;

    @JsonProperty("fechaMovimiento")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    @JsonProperty("modalidad")
    private ModeEntity modality;

    @JsonProperty("fechaAnotacionMovimiento")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fechaAnotacionMovimiento;

    @JsonProperty("txtComercio")
    private String txtComercio;

    @JsonProperty("numeroMovimintoEnDia")
    private int numeroMovimintoEnDia;

    @JsonProperty("conceptoSaldo")
    private String conceptoSaldo;

    public String getSituacionDelMovimiento() {
        return situacionDelMovimiento;
    }

    public String getDescFormaPago() {
        return descFormaPago;
    }

    public String getSituacionContratoDESSITUA() {
        return situacionContratoDESSITUA;
    }

    public int getNumSecDelMovto() {
        return numSecDelMovto;
    }

    public String getHoraMovimiento() {
        return horaMovimiento;
    }

    public String getEstadoPeticion() {
        return estadoPeticion;
    }

    public AmountEntity getImpOperacion() {
        return impOperacion;
    }

    public AmountEntity getRedondeo() {
        return redondeo;
    }

    public Date getFechaLiquidacion() {
        return fechaLiquidacion;
    }

    public String getCodOperacionBancaria() {
        return codOperacionBancaria;
    }

    public String getDescription() {
        return description;
    }

    public String getDescTipoSaldo() {
        return descTipoSaldo;
    }

    public Date getFechaOperacion() {
        return fechaOperacion;
    }

    public AmountEntity getTransactionAmount() {
        return transactionAmount;
    }

    public String getCodigoOperacionBasica() {
        return codigoOperacionBasica;
    }

    public String getIndNfc() {
        return indNfc;
    }

    public List<CategoryEntity> getCategories() {
        return categories;
    }

    public String getIndPagoFacil() {
        return indPagoFacil;
    }

    public int getNumeroOperacionDGO() {
        return numeroOperacionDGO;
    }

    public EarningCategoryEntity getCategoriaGanadora() {
        return categoriaGanadora;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public ModeEntity getModality() {
        return modality;
    }

    public Date getFechaAnotacionMovimiento() {
        return fechaAnotacionMovimiento;
    }

    public String getTxtComercio() {
        return txtComercio;
    }

    public int getNumeroMovimintoEnDia() {
        return numeroMovimintoEnDia;
    }

    public String getConceptoSaldo() {
        return conceptoSaldo;
    }

    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(transactionAmount.toTinkAmount())
                .setDescription(description)
                .setDate(transactionDate)
                .build();
    }
}
