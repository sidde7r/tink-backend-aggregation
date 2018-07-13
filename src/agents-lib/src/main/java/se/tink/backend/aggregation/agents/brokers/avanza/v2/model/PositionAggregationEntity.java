package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionAggregationEntity {
    private String instrumentType;
    private double totalValue;
    private List<PositionEntity> positions;
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
        return positions == null ? Collections.emptyList() : positions;
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
