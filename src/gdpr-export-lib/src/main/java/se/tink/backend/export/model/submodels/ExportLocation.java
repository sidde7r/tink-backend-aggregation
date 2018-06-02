package se.tink.backend.export.model.submodels;

import java.util.Date;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportLocation implements DefaultSetter {

    private final String date;
    private final double latitude;
    private final double longitude;

    public ExportLocation(Date date, double latitude, double longitude) {
        this.date = notNull(date);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
