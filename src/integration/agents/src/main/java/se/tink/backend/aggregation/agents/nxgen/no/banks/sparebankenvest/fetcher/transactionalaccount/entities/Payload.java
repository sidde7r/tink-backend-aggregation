package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload {
    private boolean canHaveMoreTransaksjoner;
    private int totalCountConsolidatedTransaksjoner;
    private Double sumOut;
    private List<TransactionEntity> transaksjoner;
    private int start;
    private int step;
    private Double sumIn;
    private int totalCount;

    public void setCanHaveMoreTransaksjoner(boolean canHaveMoreTransaksjoner) {
        this.canHaveMoreTransaksjoner = canHaveMoreTransaksjoner;
    }

    public boolean isCanHaveMoreTransaksjoner() {
        return canHaveMoreTransaksjoner;
    }

    public void setTotalCountConsolidatedTransaksjoner(int totalCountConsolidatedTransaksjoner) {
        this.totalCountConsolidatedTransaksjoner = totalCountConsolidatedTransaksjoner;
    }

    public int getTotalCountConsolidatedTransaksjoner() {
        return totalCountConsolidatedTransaksjoner;
    }

    public void setSumOut(Double sumOut) {
        this.sumOut = sumOut;
    }

    public Double getSumOut() {
        return sumOut;
    }

    public void setTransaksjoner(List<TransactionEntity> transaksjoner) {
        this.transaksjoner = transaksjoner;
    }

    public List<TransactionEntity> getTransaksjoner() {
        return transaksjoner;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStart() {
        return start;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getStep() {
        return step;
    }

    public void setSumIn(Double sumIn) {
        this.sumIn = sumIn;
    }

    public Double getSumIn() {
        return sumIn;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
