package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DateEntity {
    private String display;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date value;

    public String getDisplay() {
        return display;
    }

    public Date getValue() {
        return value;
    }
}
