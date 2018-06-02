package se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation;

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
}
