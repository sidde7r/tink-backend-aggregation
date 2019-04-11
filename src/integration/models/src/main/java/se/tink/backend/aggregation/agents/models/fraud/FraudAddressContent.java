package se.tink.backend.aggregation.agents.models.fraud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;

@JsonIgnoreProperties
public class FraudAddressContent extends FraudDetailsContent {

    private String address;
    private String postalcode;
    private String city;
    private String community;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    @Override
    public String generateContentId() {
        return String.valueOf(Objects.hash(itemType(), address, postalcode));
    }

    @Override
    public FraudTypes itemType() {
        return FraudTypes.IDENTITY;
    }
}
