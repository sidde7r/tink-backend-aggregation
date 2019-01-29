package se.tink.libraries.provider.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import org.joda.time.LocalTime;
import se.tink.libraries.date.DateUtils;

/**
 * Class for holding a a refresh schedule for a provider
 *
 * A schedule is simple a start time and and end time for when credentials are automatically refreshed against the
 * provider. The schedule is also supporting midnight overlap so a schedule can be from 22:00 to 03:00.
 */
public class ProviderRefreshSchedule implements Serializable {

    private final static int TOTAL_MILLS_PER_DAY = (int) TimeUnit.DAYS.toMillis(1);

    private LocalTime from;
    private LocalTime to;

    public ProviderRefreshSchedule() {
    }

    public ProviderRefreshSchedule(String from, String to) {
        this.from = LocalTime.parse(from);
        this.to = LocalTime.parse(to);

        Preconditions.checkState(!this.from.equals(this.to), "From and to should be different");
    }

    public void setFrom(String from) {
        this.from = LocalTime.parse(from);
    }

    public void setTo(String to) {
        this.to = LocalTime.parse(to);
    }

    @JsonProperty("from")
    public String getFromString() {
        return from.toString("HH:mm");
    }

    @JsonProperty("to")
    public String getToAsString() {
        return to.toString("HH:mm");
    }

    /**
     * Return true if the schedule is active right now
     */
    @JsonIgnore
    public boolean isActiveNow() {
        return isActiveAt(LocalTime.now());
    }

    /**
     * Return true if the schedule is active at the specified date
     */
    @JsonIgnore
    public boolean isActiveAt(LocalTime time) {
        return DateUtils.isWithinClosedInterval(from, to, time);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("from", from)
                .add("to", to)
                .add("isActive", isActiveNow())
                .toString();
    }
}
