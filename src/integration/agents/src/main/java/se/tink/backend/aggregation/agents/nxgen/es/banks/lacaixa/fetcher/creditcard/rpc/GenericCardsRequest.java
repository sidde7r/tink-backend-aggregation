package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericCardsRequest {

    public GenericCardsRequest(boolean fromBegin, int numElements) {
        this.fromBegin = fromBegin;
        this.numElements = numElements;
        this.optionFilter = LaCaixaConstants.DefaultRequestParams.OPTION_FILTER;
        this.productTypeFilter = LaCaixaConstants.DefaultRequestParams.PRODUCT_TYPE_FILTER;
        this.statusFilter = LaCaixaConstants.DefaultRequestParams.STATUS_FILTER;
        this.liquidationFilter = LaCaixaConstants.DefaultRequestParams.LIQUIDATION_FILTER;
    }

    @JsonProperty("filtro_opcion")
    private String optionFilter;

    @JsonProperty("filtro_tipoproducto")
    private String productTypeFilter;

    @JsonProperty("filtro_estado")
    private String statusFilter;

    @JsonProperty("filtro_liquidacion")
    private String liquidationFilter;

    @JsonProperty("inicio")
    private boolean fromBegin;

    @JsonProperty("numelementos")
    private int numElements;
}
