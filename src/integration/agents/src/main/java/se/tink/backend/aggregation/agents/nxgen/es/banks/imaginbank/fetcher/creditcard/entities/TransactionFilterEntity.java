package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionFilterEntity {
    private static final java.time.format.DateTimeFormatter JAVA_LOCAL_DATE_IMAGINBANK_FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("dd\\/MM\\/yyyy");

    @JsonProperty("filtro_fecha_inicio")
    private String filterStartDate;
    @JsonProperty("filtro_importe_minimo")
    private String filtroImporteMinimo = "";
    @JsonProperty("filtro_estado")
    private String filtroEstado = "";
    @JsonProperty("filtro_categoria")
    private String filtroCategoria = "";
    @JsonProperty("filtro_etiqueta")
    private String filtroEtiqueta = "";
    @JsonProperty("filtro_importe_maximo")
    private String filtroImporteMaximo = "";
    @JsonProperty("filtro_comercio")
    private String filtroComercio = "";
    @JsonProperty("filtro_tarjetas")
    private String filterCards;
    @JsonProperty("filtro_fecha_fin")
    private String filterEndDate;

    public static final TransactionFilterEntity createTransactionFilter(String filterCards,
            LocalDate startDate,
            LocalDate endDate) {
        TransactionFilterEntity filter = new TransactionFilterEntity();
        filter.filterCards = filterCards;
        filter.filterStartDate = startDate.format(JAVA_LOCAL_DATE_IMAGINBANK_FORMATTER);
        filter.filterEndDate = endDate.format(JAVA_LOCAL_DATE_IMAGINBANK_FORMATTER);

        return filter;
    }
}
