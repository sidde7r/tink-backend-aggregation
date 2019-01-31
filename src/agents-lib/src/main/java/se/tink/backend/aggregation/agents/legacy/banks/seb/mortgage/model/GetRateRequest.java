package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.ApiRequest;

/**
 * Query string request model for GET: indicative interest rate
 * <p>
 * loan_amount is a mandatory field. Value has to be greater than 0<br />
 * age is a mandatory field. Value has to be greater than 0<br />
 * property_type: 01 = appartment; 02 = villa; 03 = others
 */
public class GetRateRequest implements ApiRequest {

    // Required
    private Integer age;
    private Double loanAmount;
    private PropertyType propertyType;

    // Optional
    private String municipality;
    private Double marketValue;
    private Double newPlacementVolume;

    @Override
    public String getUriPath() {
        return "/rates?" + toQueryString();
    }

    private String toQueryString() {
        Escaper queryEscaper = UrlEscapers.urlFormParameterEscaper();

        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("age=").append(age);
        queryBuilder.append("&loan_amount=").append(doubleToString(loanAmount));
        queryBuilder.append("&property_type=").append(propertyType.getNumericalKey());

        if (municipality != null) {
            queryBuilder.append("&municipality=").append(queryEscaper.escape(municipality));
        }
        if (marketValue != null) {
            queryBuilder.append("&market_value=").append(doubleToString(marketValue));
        }
        if (newPlacementVolume != null) {
            queryBuilder.append("&new_placement_volume=").append(doubleToString(newPlacementVolume));
        }

        return queryBuilder.toString();
    }

    private static String doubleToString(double value) {
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
        DecimalFormat decimalFormat = new DecimalFormat("0.00", decimalFormatSymbols);
        return decimalFormat.format(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GetRateRequest that = (GetRateRequest) o;

        return Objects.equal(this.age, that.age) &&
                Objects.equal(this.loanAmount, that.loanAmount) &&
                Objects.equal(this.propertyType, that.propertyType) &&
                Objects.equal(this.municipality, that.municipality) &&
                Objects.equal(this.marketValue, that.marketValue) &&
                Objects.equal(this.newPlacementVolume, that.newPlacementVolume);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(age, loanAmount, propertyType, municipality, marketValue, newPlacementVolume);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("age", age)
                .add("loanAmount", loanAmount)
                .add("propertyType", propertyType)
                .add("municipality", municipality)
                .add("marketValue", marketValue)
                .add("newPlacementVolume", newPlacementVolume)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final GetRateRequest getRateRequest;

        private Builder() {
            getRateRequest = new GetRateRequest();
        }

        public Builder withLoanAmount(Double loanAmount) {
            Preconditions.checkArgument(loanAmount != null);
            getRateRequest.loanAmount = loanAmount;
            return this;
        }

        public Builder withAge(Integer age) {
            Preconditions.checkArgument(age != null);
            getRateRequest.age = age;
            return this;
        }

        public Builder withPropertyType(PropertyType propertyType) {
            Preconditions.checkArgument(propertyType != null);
            getRateRequest.propertyType = propertyType;
            return this;
        }

        public Builder withOptionalMunicipality(String municipality) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(municipality), "Construction with no string not needed");
            getRateRequest.municipality = municipality;
            return this;
        }

        public Builder withOptionalMarketValue(double marketValue) {
            getRateRequest.marketValue = marketValue;
            return this;
        }

        public Builder withOptionalNewPlacementVolume(double newPlacementVolume) {
            getRateRequest.newPlacementVolume = newPlacementVolume;
            return this;
        }

        public GetRateRequest build() {
            Preconditions.checkArgument(getRateRequest.age != null);
            Preconditions.checkArgument(getRateRequest.loanAmount!= null);
            Preconditions.checkArgument(getRateRequest.propertyType != null);
            Preconditions.checkArgument(getRateRequest.propertyType.getNumericalKey() != null);

            return getRateRequest;
        }
    }
}
