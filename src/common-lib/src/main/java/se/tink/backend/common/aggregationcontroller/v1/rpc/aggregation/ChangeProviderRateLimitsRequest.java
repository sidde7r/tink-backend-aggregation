package se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation;

import java.util.Map;

public class ChangeProviderRateLimitsRequest {
    private double defaultRate;
    private Map<String, Double> ratePerSecondByClassname;

    public double getDefaultRate() {
        return defaultRate;
    }

    public Map<String, Double> getRatePerSecondByClassname() {
        return ratePerSecondByClassname;
    }

    public void setDefaultRate(double defaultRate) {
        this.defaultRate = defaultRate;
    }

    public void setRatePerSecondByClassname(Map<String, Double> ratePerSecondByClassname) {
        this.ratePerSecondByClassname = ratePerSecondByClassname;
    }

    public se.tink.backend.aggregation.rpc.ChangeProviderRateLimitsRequest
    toAggregationChangeProviderRateLimitsRequest() {
        se.tink.backend.aggregation.rpc.ChangeProviderRateLimitsRequest changeProviderRateLimitsRequest =
                new se.tink.backend.aggregation.rpc.ChangeProviderRateLimitsRequest();

        changeProviderRateLimitsRequest.setDefaultRate(this.defaultRate);
        changeProviderRateLimitsRequest.setRatePerSecondByClassname(this.ratePerSecondByClassname);

        return changeProviderRateLimitsRequest;
    }
}
