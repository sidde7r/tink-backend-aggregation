package se.tink.backend.core.follow;

import io.protostuff.Tag;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;

import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.Transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FollowData {
    @Tag(1)
    @ApiModelProperty(name="averagePeriodAmounts", hidden = true)
    private List<StringDoublePair> averagePeriodAmounts;
    @Tag(2)
    @ApiModelProperty(name = "historicalAmounts", value="A list of string-double pairs. Has one value for the sum of expenses (ie. EXPENSES or SEARCH type) or account balance (ie. SAVINGS type) for each previous month period (MONTHLY or MONTHLY_ADJUSTED).")
    private List<StringDoublePair> historicalAmounts;
    @Tag(3)
    @ApiModelProperty(name = "period", value="The period (yyyy-mm) for which this data belongs to. Period is always MONTHLY or MONTHLY_ADJUSTED.", example = "2017-05")
    private String period;
    @Tag(4)
    @ApiModelProperty(name = "periodAmounts", value="A list of string-double pairs. Has one value for the cumulative sum of expenses (ie. EXPENSES or SEARCH type) or account balance (ie. SAVINGS type) for each day in period.")
    private List<StringDoublePair> periodAmounts;
    @Tag(5)
    @ApiModelProperty(name = "periodProgress", value="A progress indicator between 0 and 1 of the current period. First day of period is 0 and last day of period is 1.", example = "0.25")
    private Double periodProgress;
    @Tag(6)
    @ApiModelProperty(name = "periodTransactions", value="A list of transactions that this Follow Item and period consists of.")
    private List<Transaction> periodTransactions;

    public FollowData () {
        
    }
    public FollowData(String period) {
        this.period = period;
    }
    
    public List<StringDoublePair> getAveragePeriodAmounts() {
        return averagePeriodAmounts;
    }

    public List<StringDoublePair> getHistoricalAmounts() {
        return historicalAmounts;
    }

    public String getPeriod() {
        return period;
    }
    
    public List<StringDoublePair> getPeriodAmounts() {
        return periodAmounts;
    }

    public Double getPeriodProgress() {
        return periodProgress;
    }

    public List<Transaction> getPeriodTransactions() {
        return periodTransactions;
    }

    public void setAveragePeriodAmounts(List<StringDoublePair> averagePeriodAmounts) {
        this.averagePeriodAmounts = averagePeriodAmounts;
    }

    public void setHistoricalAmounts(List<StringDoublePair> historicalAmounts) {
        this.historicalAmounts = historicalAmounts;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public void setPeriodAmounts(List<StringDoublePair> periodAmounts) {
        this.periodAmounts = periodAmounts;
    }

    public void setPeriodProgress(Double periodProgress) {
        this.periodProgress = periodProgress;
    }

    public void setPeriodTransactions(List<Transaction> periodTransactions) {
        this.periodTransactions = periodTransactions;
    }
     
    @JsonIgnore
	public double getCurrentAmount()
	{
		if (historicalAmounts.size() > 0)
		{
            for (StringDoublePair historicalAmount : historicalAmounts) {
                if (historicalAmount.getKey().equals(period)) {
                    return historicalAmount.getValue();
                }
            }
        }
        return 0;
    }
}
