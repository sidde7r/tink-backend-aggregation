package se.tink.analytics.jobs.loandata;

import java.util.UUID;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.broadcast.Broadcast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoanDataToAggregatedAreaLoanData implements FlatMapFunction<LoanData, AggregatedAreaLoanDataExtended> {

    private Broadcast<Map<UUID, UUID>> areaByUser;

    public LoanDataToAggregatedAreaLoanData(Broadcast<Map<UUID, UUID>> areaByUser) {

        this.areaByUser = areaByUser;
    }

    @Override
    public Iterable<AggregatedAreaLoanDataExtended> call(LoanData data) throws Exception {

        List<AggregatedAreaLoanDataExtended> list = new ArrayList<>();

        UUID areaId = areaByUser.value().get(data.getUserId());

        AggregatedAreaLoanDataExtended all = new AggregatedAreaLoanDataExtended();
        all.setAreaId(areaId);
        all.setUserId(data.getUserId());
        all.setBank("all");
        all.setAvgInterest(data.getInterest());
        all.setAvgBalance(data.getBalance());

        list.add(all);

        AggregatedAreaLoanDataExtended provider = new AggregatedAreaLoanDataExtended();
        provider.setAreaId(areaId);
        provider.setUserId(data.getUserId());
        provider.setBank(cleanUpProviderName(data.getProviderName()));
        provider.setAvgInterest(data.getInterest());
        provider.setAvgBalance(data.getBalance());

        list.add(provider);

        return list;
    }

    private String cleanUpProviderName(String providerName) {
        if (providerName.endsWith("-ssn-bankid")) {
            return providerName.substring(0, providerName.indexOf("-ssn-bankid"));
        }
        if (providerName.endsWith("-bankid")) {
            return providerName.substring(0, providerName.indexOf("-bankid"));
        }
        return providerName;
    }
}
