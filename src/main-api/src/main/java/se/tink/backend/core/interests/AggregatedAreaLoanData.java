package se.tink.backend.core.interests;

import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public class AggregatedAreaLoanData implements Serializable {

    private double avgInterest;
    private double avgBalance;
    private String bank;
    private String bankDisplayName;
    private long numLoans;
    private long numUsers;
    private UUID areaId;

    public static Map<String, String> getColumnMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put("avgInterest", "avginterest");
        map.put("avgBalance", "avgbalance");
        map.put("bankDisplayName", "bankdisplayname");
        map.put("numLoans", "numloans");
        map.put("numUsers", "numusers");
        map.put("areaId", "areaid");
        return map;
    }

    public double getAvgInterest() {
        return avgInterest;
    }

    public void setAvgInterest(double avgInterest) {
        this.avgInterest = avgInterest;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBankDisplayName() {
        return bankDisplayName;
    }

    public void setBankDisplayName(String bankDisplayName) {
        this.bankDisplayName = bankDisplayName;
    }

    public long getNumLoans() {
        return numLoans;
    }

    public void setNumLoans(long numLoans) {
        this.numLoans = numLoans;
    }

    public long getNumUsers() {
        return numUsers;
    }

    public void setNumUsers(long numUsers) {
        this.numUsers = numUsers;
    }

    public double getAvgBalance() {
        return avgBalance;
    }

    public void setAvgBalance(double avgBalance) {
        this.avgBalance = avgBalance;
    }

    public UUID getAreaId() {
        return areaId;
    }

    public void setAreaId(UUID areaId) {
        this.areaId = areaId;
    }
}
