package se.tink.backend.aggregation.agents.models.fraud;

import java.util.Date;
import java.util.Objects;

public class FraudRealEstateEngagementContent extends FraudDetailsContent {

    private String name;
    private String muncipality;
    private String number;
    private Date acquisitionDate;
    private double assessedValue;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getMuncipality() {
        return muncipality;
    }
    public void setMuncipality(String muncipality) {
        this.muncipality = muncipality;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public Date getAcquisitionDate() {
        return acquisitionDate;
    }
    public void setAcquisitionDate(Date acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }
    public double getAssessedValue() {
        return assessedValue;
    }
    public void setAssessedValue(double assessedValue) {
        this.assessedValue = assessedValue;
    }

    @Override
    public String generateContentId() {
        return String.valueOf(Objects.hash(itemType(), name, muncipality, number));
    }
    
    @Override
    public FraudTypes itemType() {
        return FraudTypes.IDENTITY;
    }
}
