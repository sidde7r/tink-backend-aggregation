package se.tink.backend.system.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;

public class ReplayQueueRequest {

    private Date fromDate;
    private Date toDate;
    @JsonIgnore
    private Date startReplayDate;

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getStartReplayDate() {
        return startReplayDate;
    }

    public void setStartReplayDate(Date startReplayDate) {
        this.startReplayDate = startReplayDate;
    }
}
