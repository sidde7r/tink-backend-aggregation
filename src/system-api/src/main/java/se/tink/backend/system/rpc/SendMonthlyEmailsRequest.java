package se.tink.backend.system.rpc;

public class SendMonthlyEmailsRequest {

    /**
     * Optional. If not used, today's date is used.
     */
    private String date;

    private boolean dryRun = false;

    /**
     * Optional. UserId to start from, excluding it.
     */
    private String startFromUserId;

    public String getDate() {
        return date;
    }

    public String getStartFromUserId() {
        return startFromUserId;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public void setStartFromUserId(String startFromUserId) {
        this.startFromUserId = startFromUserId;
    }

}
