package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.protostuff.Tag;

public class MinimizedStatistic {

    @Tag(1)
    private String description;

    @Tag(2)
    private String period;

    @Tag(3)
    private double value;

    public String getDescription() {
        return description;
    }

    public String getPeriod() {
        return period;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(period, value, description);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof MinimizedStatistic) {
            final MinimizedStatistic other = (MinimizedStatistic) obj;

            return (Objects.equal(period, other.period)
                    && Objects.equal(
                    description, other.description) && Objects.equal(value, other.value));
        } else {
            return false;
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("period", period)
                .add("description", description).add("value", value)
                .toString();
    }

}
