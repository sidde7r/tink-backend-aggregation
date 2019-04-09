package se.tink.libraries.date;

import com.google.common.base.Predicate;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class Period implements Serializable {
    @Tag(1)
    @ApiModelProperty(hidden = true)
    private boolean clean;

    @Tag(2)
    @ApiModelProperty(value = "Timestamp at the end of the period", example = "1464739199000")
    private Date endDate;

    @Tag(3)
    @ApiModelProperty(example = "2016-05")
    private String name;

    @Tag(4)
    @ApiModelProperty(
            value = "Resolution for the statistics. ",
            allowableValues = ResolutionTypes.PERIOD_MODE_DOCUMENTED,
            example = "MONTHLY")
    private ResolutionTypes resolution;

    @Tag(5)
    @ApiModelProperty(value = "Timestamp at the start of the period", example = "1462060800000")
    private Date startDate;

    public static final Predicate<Period> PERIOD_IS_CLEAN = Period::isClean;

    public Period() {}

    public Period(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getName() {
        return name;
    }

    public ResolutionTypes getResolution() {
        return resolution;
    }

    public Date getStartDate() {
        return startDate;
    }

    public boolean isClean() {
        return clean;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setResolution(ResolutionTypes resolution) {
        this.resolution = resolution;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return String.format(
                "{name: %s, resolution: %s, startDate: %s, endDate: %s, clean: %s}",
                name, resolution, startDate, endDate, clean);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Period)) {
            return false;
        }

        Period p = (Period) obj;
        return name.equals(p.getName())
                && resolution.toString().equals(p.getResolution().toString())
                && startDate.equals(p.getStartDate())
                && endDate.equals(p.getEndDate())
                && clean == p.isClean();
    }

    @Override
    public int hashCode() {
        int result = (clean ? 1 : 0);
        result = 31 * result + endDate.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + resolution.hashCode();
        result = 31 * result + startDate.hashCode();
        return result;
    }

    public boolean isDateWithin(Date date) {
        return date.after(startDate) && date.before(endDate);
    }

    public boolean isDateWithinInclusive(Date date) {
        return !date.after(endDate) && !date.before(startDate);
    }

    public boolean isPeriodWithinInclusive(Period period) {
        return this.isDateWithinInclusive(period.getStartDate())
                && this.isDateWithinInclusive(period.getEndDate());
    }
}
