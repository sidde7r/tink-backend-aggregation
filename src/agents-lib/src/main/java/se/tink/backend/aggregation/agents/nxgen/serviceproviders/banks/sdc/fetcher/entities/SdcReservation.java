package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;

/*
    {
      "id": "1122411431159137333346",
      "type": "VPES",
      "amount": {
        "localizedValue": "-33.00",
        "localizedValueWithCurrency": "-33.00 NOK",
        "value": -3300,
        "scale": 2,
        "currency": "NOK",
        "localizedValueWithCurrencyAtEnd": "-33.00 NOK",
        "roundedAmountWithIsoCurrency": "-NOK33",
        "roundedAmountWithCurrencySymbol": "-33 NOK"
      },
      "calculatedAmount": {
        "localizedValue": "9,405.82",
        "localizedValueWithCurrency": "9,405.82",
        "value": 940582,
        "scale": 2,
        "currency": null,
        "localizedValueWithCurrencyAtEnd": null,
        "roundedAmountWithIsoCurrency": null,
        "roundedAmountWithCurrencySymbol": null
      },
      "expirationDate": "2017-12-05",
      "description": "Vipps by DnB/OSLO/NO",
      "hostId": "112358767144769",
      "createDate": "2017-11-30",
      "createTimestamp": "2017-11-30T09:03:46",
      "status": "ACTIVE",
      "validation": "UNKN",
      "labelValuePair": [
        {
          "label": "Amount",
          "value": "-33.00"
        },
        {
          "label": "Reservation number",
          "value": "1122411431159137333346"
        },
        {
          "label": "Created on",
          "value": "2017-11-30"
        },
        {
          "label": "Expiration date",
          "value": "2017-12-05"
        },
        {
          "label": "Status",
          "value": "aktiv"
        }
      ]
    }

 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SdcReservation {
    private String id;
    private String type;
    private SdcAmount amount;
    private SdcAmount calculatedAmount;
    private String expirationDate;
    private String description;
    private String hostId;
    private String createDate;
    private String createTimestamp;
    private String status;
    private String validation;
    private ArrayList<LabelValuePair> labelValuePair;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public SdcAmount getAmount() {
        return amount;
    }

    public SdcAmount getCalculatedAmount() {
        return calculatedAmount;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getDescription() {
        return description;
    }

    public String getHostId() {
        return hostId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getCreateTimestamp() {
        return createTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getValidation() {
        return validation;
    }

    public ArrayList<LabelValuePair> getLabelValuePair() {
        return labelValuePair;
    }
}
