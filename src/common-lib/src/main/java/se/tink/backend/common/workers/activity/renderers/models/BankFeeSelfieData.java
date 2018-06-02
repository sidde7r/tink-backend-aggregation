package se.tink.backend.common.workers.activity.renderers.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;
import se.tink.backend.core.BankFeeType;

import java.util.Date;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankFeeSelfieData {
    private Double averageSpendingInTink;
    private Map<BankFeeType, Double> spendingByType;
    private Date newestTransactionDate;

    public Map<BankFeeType, Double> getSpendingByType() {
        return spendingByType;
    }

    public void setFeesByType(Map<BankFeeType, Double> spendingByType) {
        this.spendingByType = spendingByType;
    }

    public Double getAverageSpendingInTink() {
        return averageSpendingInTink;
    }

    public void setAverageSpendingInTink(Double averageSpendingInTink) {
        this.averageSpendingInTink = averageSpendingInTink;
    }

    public Date getNewestTransactionDate() {
        return newestTransactionDate;
    }

    public void setNewestTransactionDate(Date newestTransactionDate) {
        this.newestTransactionDate = newestTransactionDate;
    }

    public double getTotal() {
        if (spendingByType == null) {
            return 0;
        }

        double total = 0;
        for (Double d : spendingByType.values()) {
            total += d;
        }
        return total;
    }

    public boolean isEmpty() {
        return Math.abs(getTotal()) < 0.00001;
    }

    public boolean isNewestTransactionOlderThan(ReadablePeriod period) {
        return newestTransactionDate != null && newestTransactionDate.before(DateTime.now().minus(period).toDate());
    }

}
