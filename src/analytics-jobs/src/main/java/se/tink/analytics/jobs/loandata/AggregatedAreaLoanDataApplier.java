package se.tink.analytics.jobs.loandata;

import java.util.Map;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;
import se.tink.analytics.spark.functions.SparkFunctions;
import se.tink.backend.core.interests.AggregatedAreaLoanData;

public class AggregatedAreaLoanDataApplier implements Function<AggregatedAreaLoanDataExtended, AggregatedAreaLoanData> {

    private Broadcast<Map<String, Object>> countByAreaAndBank;
    private Broadcast<Map<String, Object>> broadcastUniqueUserCount;

    public AggregatedAreaLoanDataApplier(
            Broadcast<Map<String, Object>> loanCountByAreaAndBank,
            Broadcast<Map<String, Object>> uniqueUserCountByAreaAndBank) {

        this.countByAreaAndBank = loanCountByAreaAndBank;
        this.broadcastUniqueUserCount = uniqueUserCountByAreaAndBank;
    }

    @Override
    public AggregatedAreaLoanData call(AggregatedAreaLoanDataExtended data) throws Exception {

        String key = SparkFunctions.Mappers.AGGREGATED_LOAN_BY_AREA_AND_BANK.call(data);

        Long loanCount = (Long) countByAreaAndBank.value().get(key);
        Long uniqueUserCount = (Long) broadcastUniqueUserCount.value().get(key);

        if (loanCount == null) {
            throw new IllegalStateException("Count cannot be null");
        }

        double sumInterest = data.getAvgInterest();
        double sumBalance = data.getAvgBalance();

        data.setAvgInterest(sumInterest / loanCount);
        data.setAvgBalance(sumBalance / uniqueUserCount);

        data.setNumLoans(loanCount);
        data.setNumUsers(uniqueUserCount);

        data.setBankDisplayName(getDisplayName(data.getBank()));

        return data;
    }

    private String getDisplayName(String bank) {
        if ("handelsbanken".equals(bank)) {
            return "Handelsbanken";
        } else if ("danskebank".equals(bank)) {
            return "Danske Bank";
        } else if ("seb".equals(bank)) {
            return "SEB";
        } else if ("nordea".equals(bank)) {
            return "Nordea";
        } else if ("swedbank".equals(bank)) {
            return "Swedbank";
        } else if ("savingsbank".equals(bank)) {
            return "Sparbanken";
        } else if ("lansforsakringar".equals(bank)) {
            return "Länsförsäkringar";
        } else if ("sbab".equals(bank)) {
            return "SBAB";
        } else if ("all".equals(bank)) {
            return "All";
        }

        return null;
    }
}
