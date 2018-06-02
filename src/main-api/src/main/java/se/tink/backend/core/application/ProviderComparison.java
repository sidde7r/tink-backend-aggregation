package se.tink.backend.core.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderComparison {

    private String imageUrl;
    private String description;
    private List<String> details;
    private double interestRate;
    private String interestRateAsPercent;
    private String providerDisplayName;
    private String cost;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getDetails() {
        return details;
    }

    public void addDetails(String detailsToAdd) {
        if (details == null) {
            details = Lists.newArrayList();
        }

        details.add(detailsToAdd);
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public String getInterestRateAsPercent() {
        return interestRateAsPercent;
    }

    public void setInterestRateAsPercent(String interestRateAsPercent) {
        this.interestRateAsPercent = interestRateAsPercent;
    }

    public String getProviderDisplayName() {
        return providerDisplayName;
    }

    public void setProviderDisplayName(String providerDisplayName) {
        this.providerDisplayName = providerDisplayName;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }
}
