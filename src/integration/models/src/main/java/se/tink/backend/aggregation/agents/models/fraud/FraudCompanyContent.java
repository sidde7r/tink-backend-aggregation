package se.tink.backend.aggregation.agents.models.fraud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FraudCompanyContent extends FraudDetailsContent {

    private String orgNumber;
    private String name;
    private String address;
    private String town;
    private String zipcode;
    private String ftax;
    private String moms;
    private String latestRegname;
    private String type;
    private String status;
    private String changeValue;
    private String changeProperty;
    private List<FraudCompanyDirector> directors;
    private int creditRating;
    private Integer nbrEmployees;
    private Integer revenue;
    private Integer profitAfterTax;
    private Integer netProfit;
    private Integer totalCapital;
    private String grossProfitMarginPercentage;
    private String quickRatioPercent;
    private String solidityPercentage;

    @Override
    public String generateContentId() {
        StringBuffer directorsBuffer = new StringBuffer();
        if (directors != null) {
            for (FraudCompanyDirector director : directors) {
                directorsBuffer.append(director.getName());
            }
        }
        return String.valueOf(
                Objects.hash(
                        itemType(),
                        orgNumber,
                        name,
                        address,
                        town,
                        zipcode,
                        ftax,
                        moms,
                        latestRegname,
                        type,
                        status,
                        creditRating,
                        nbrEmployees,
                        revenue,
                        profitAfterTax,
                        netProfit,
                        totalCapital,
                        grossProfitMarginPercentage,
                        quickRatioPercent,
                        solidityPercentage,
                        directorsBuffer.toString()));
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getFtax() {
        return ftax;
    }

    public void setFtax(String ftax) {
        this.ftax = ftax;
    }

    public String getLatestRegname() {
        return latestRegname;
    }

    public void setLatestRegname(String latestRegname) {
        this.latestRegname = latestRegname;
    }

    public void setDirectors(List<FraudCompanyDirector> directors) {
        this.directors = directors;
    }

    @Override
    public FraudTypes itemType() {
        return FraudTypes.IDENTITY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrgNumber() {
        return orgNumber;
    }

    public void setOrgNumber(String orgNumber) {
        this.orgNumber = orgNumber;
    }

    public String getMoms() {
        return moms;
    }

    public void setMoms(String moms) {
        this.moms = moms;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChangeValue() {
        return changeValue;
    }

    public void setChangeValue(String changeValue) {
        this.changeValue = changeValue;
    }

    public String getChangeProperty() {
        return changeProperty;
    }

    public void setChangeProperty(String changeProperty) {
        this.changeProperty = changeProperty;
    }

    public int getCreditRating() {
        return creditRating;
    }

    public void setCreditRating(int creditRating) {
        this.creditRating = creditRating;
    }

    public Integer getProfitAfterTax() {
        return profitAfterTax;
    }

    public void setProfitAfterTax(Integer profitAfterTax) {
        this.profitAfterTax = profitAfterTax;
    }

    public Integer getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(Integer netProfit) {
        this.netProfit = netProfit;
    }

    public Integer getTotalCapital() {
        return totalCapital;
    }

    public void setTotalCapital(Integer totalCapital) {
        this.totalCapital = totalCapital;
    }

    public Integer getRevenue() {
        return revenue;
    }

    public void setRevenue(Integer revenue) {
        this.revenue = revenue;
    }

    public String getGrossProfitMarginPercentage() {
        return grossProfitMarginPercentage;
    }

    public void setGrossProfitMarginPercentage(String grossProfitMarginPercentage) {
        this.grossProfitMarginPercentage = grossProfitMarginPercentage;
    }

    public String getQuickRatioPercent() {
        return quickRatioPercent;
    }

    public void setQuickRatioPercent(String quickRatioPercent) {
        this.quickRatioPercent = quickRatioPercent;
    }

    public String getSolidityPercentage() {
        return solidityPercentage;
    }

    public void setSolidityPercentage(String solidityPercentage) {
        this.solidityPercentage = solidityPercentage;
    }

    public Integer getNbrEmployees() {
        return nbrEmployees;
    }

    public void setNbrEmployees(Integer nbrEmployees) {
        this.nbrEmployees = nbrEmployees;
    }
}
