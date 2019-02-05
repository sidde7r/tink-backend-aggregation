package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentEntity {
    private String instrumentType;
    private List<PositionEntity> positions;
    private double totalValue;
    private double todaysProfitPercent;
    private double totalProfitValue;
    private double totalProfitPercent;

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public List<PositionEntity> getPositions() {
        return Optional.ofNullable(positions).orElse(Collections.emptyList());
    }

    public void setPositions(List<PositionEntity> positions) {
        this.positions = positions;
    }

    public double getTodaysProfitPercent() {
        return todaysProfitPercent;
    }

    public void setTodaysProfitPercent(double todaysProfitPercent) {
        this.todaysProfitPercent = todaysProfitPercent;
    }

    public double getTotalProfitValue() {
        return totalProfitValue;
    }

    public void setTotalProfitValue(double totalProfitValue) {
        this.totalProfitValue = totalProfitValue;
    }

    public double getTotalProfitPercent() {
        return totalProfitPercent;
    }

    public void setTotalProfitPercent(double totalProfitPercent) {
        this.totalProfitPercent = totalProfitPercent;
    }
}
