package se.tink.backend.core;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "portfolios")
public class Portfolio {

    public enum Type {
        ISK, KF, DEPOT, PENSION, OTHER;

        public static final String DOCUMENTED = "ISK, KF, DEPOT, PENSION, OTHER";
    }

    @PrimaryKeyColumn(ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;
    @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID accountId;
    @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID id;

    // For an account with one portfolio this can be the account nr. For accounts with multiple portfolios this can be
    // the account nr combined with some static name/type/id only applicable to this portfolio.
    private String uniqueIdentifier;

    // The total profit of the entire portfolio, both historical (real) profit and current (potential) profit.
    private Double totalProfit;
    // The funds, on this portfolio, available for purchase instruments.
    private Double cashValue;

    // Instruments are saved in a separate table.
    @Transient
    private List<Instrument> instruments;

    @Transient
    private Credentials credentials;
    private Double totalValue;
    private Type type;
    private String rawType;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public Double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(Double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getRawType() {
        return rawType;
    }

    public void setRawType(String rawType) {
        this.rawType = rawType;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public List<Instrument> getInstruments() {
        if (instruments == null) {
            return Collections.emptyList();
        }

        return instruments;
    }

    public void setInstruments(List<Instrument> instruments) {
        this.instruments = instruments;
    }

    public Double getCashValue() {
        return cashValue;
    }

    public void setCashValue(Double cashValue) {
        this.cashValue = cashValue;
    }
}
