package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import java.util.List;

/*
{
"label" : "Market Value",
"marketValue" : {
    "localizedValue" : "10.00",
    "localizedValueWithCurrency" : "10.00 SEK",
    "value" : 1000,
    "scale" : 2,
    "currency" : "SEK",
    "localizedValueWithCurrencyAtEnd" : "10.00 SEK",
    "roundedAmountWithIsoCurrency" : "SEK10",
    "roundedAmountWithCurrencySymbol" : "10 SEK"
},

"labelValuePairs" : [ {
        "label" : "Share units",
        "value" : "0.011230"
    }, {
        "label" : "Share values",
        "value" : "890.51"
    }, {
        "label" : "Share value date",
        "value" : "2017-12-06"
    }, {
        "label" : "Purchase Price",
        "value" : "10.00"
    }, {
        "label" : "Value Change (%)",
        "value" : "0 %"
    }, {
        "label" : "Value Change",
        "value" : "0.00"
    } ]
}
 */
public class SdcDepositDetails {
    private String label;
    private SdcAmount marketValue;
    private List<LabelValuePair> labelValuePairs;

    public String getLabel() {
        return label;
    }

    public SdcAmount getMarketValue() {
        return marketValue;
    }

    public List<LabelValuePair> getLabelValuePairs() {
        return labelValuePairs;
    }
}
