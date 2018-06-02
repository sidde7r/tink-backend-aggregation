package se.tink.backend.core;

import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import se.tink.libraries.date.ResolutionTypes;

public class StatisticQuery {
    @Tag(1)
    @ApiModelProperty(name = "description", value="Identifier of the data the statistic represents.", example = "fe9e199c2ca94c12baf1f3eb4a4122de")
    private String description;
    @Tag(2)
    @ApiModelProperty(name = "periods", value="Time periods for the statistics: year, month, week or day. Format: '2014', '2014-02', 2014:45 or '2014-02-12'", example = "[\"2014-02-11\",\"2014-02-12\"]")
    private List<String> periods;
    @Tag(3)
    @ApiModelProperty(name = "resolution", value="Resolution for the statistics. Note that monthly statistics will be calculated only with the resolution that the user has in the user settings (" + ResolutionTypes.PERIOD_MODE_DOCUMENTED + "), and not for both.", example = "DAILY")
    private ResolutionTypes resolution;
    @Tag(4)
    @ApiModelProperty(name = "types", value="A list of types of statistics. See Statistics for type information.", example = "[\"expenses-by-category\"]")
    private List<String> types;
    @Tag(5)
    @ApiModelProperty(name = "padResultUntilToday", value="Indicates if the result should be flat filled until the period of today.", example = "true")
    private boolean padResultUntilToday;

    public String getDescription() {
        return description;
    }

    public List<String> getPeriods() {
        return periods;
    }

    public ResolutionTypes getResolution() {
        return resolution;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPeriods(List<String> periods) {
        this.periods = periods;
    }

    public void setResolution(ResolutionTypes resolution) {
        this.resolution = resolution;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public boolean getPadResultUntilToday() {
        return padResultUntilToday;
    }

    public void setPadResultUntilToday(boolean padResultUntilToday) {
        this.padResultUntilToday = padResultUntilToday;
    }
}
