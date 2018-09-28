package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericCardsRequest {

    public GenericCardsRequest(boolean fromBegin, int numElements) {
        this.fromBegin = fromBegin;
        this.numElements = numElements;
        this.optionFilter = ImaginBankConstants.DefaultRequestParams.OPTION_FILTER;
        this.productTypeFilter = ImaginBankConstants.DefaultRequestParams.PRODUCT_TYPE_FILTER;
        this.statusFilter = ImaginBankConstants.DefaultRequestParams.STATUS_FILTER;
        this.liquidationFilter = ImaginBankConstants.DefaultRequestParams.LIQUIDATION_FILTER;
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
