package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingPlansEntity {
    private AmountEntity accumulatedAmount;
    private String certifiedNumber;
    private String codIban;
    private String code;
    private String contractCode;
    private String contractEntity;
    private String contractNumber;
    private String currencyCode;
    private String date1;
    private String date2;
    private String descProd;
    private String effectiveDate;
    private String entityCode;
    private String group;
    private String insuranceName;
    private String interestRate;
    private String name;
    private String productCode;
    private String productDesc;
    private String productType;
    private boolean type;

    @JsonIgnore
    public Map<String, String> getQueryParamsForDetailsRequest() {
        Map<String, String> queryParams = new HashMap<>();

        queryParams.put(SabadellConstants.QueryParamsKeys.CONTRACT_CODE, contractCode);
        queryParams.put(SabadellConstants.QueryParamsKeys.ENTITY_CODE, entityCode);
        queryParams.put(SabadellConstants.QueryParamsKeys.PRODUCT_TYPE, productType);

        return queryParams;
    }
}
