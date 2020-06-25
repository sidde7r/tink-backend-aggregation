package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class HandelsbankenSEPensionInfo extends BaseResponse {

    private String additionalText;

    private String specialText;

    private HandelsbankenSEPensionValue value;

    private String key;

    public String getAdditionalText() {
        return additionalText;
    }

    public String getSpecialText() {
        return specialText;
    }

    public HandelsbankenSEPensionValue getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public URL toPensionDetail() {
        return findLink(HandelsbankenConstants.URLS.Links.PENSION_DETAILS);
    }
}
