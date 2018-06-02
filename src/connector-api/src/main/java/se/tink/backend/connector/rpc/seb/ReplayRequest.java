package se.tink.backend.connector.rpc.seb;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;

public class ReplayRequest {
    private List<String> userTokens;
    private Date fromDate;
    private Date toDate;

    public ReplayRequest() {
    }

    public ReplayRequest(Iterable<String> userTokens, Date fromDate, Date toDate) {
        this.userTokens = Lists.newArrayList(userTokens);
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public Iterable<String> getUserTokens() {
        return userTokens;
    }

    public void setUserTokens(List<String> userTokens) {
        this.userTokens = userTokens;
    }

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
}
