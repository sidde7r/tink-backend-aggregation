package se.tink.backend.common.search.strategies;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

/**
 * Renames properties with the name "latitude" to "lat" and "longitude" to "lon" to be able to use the
 * geographical queries in elastic search. Elastic search required that the field names are
 * lat and lon.
 */
public class CoordinateNamingStrategy extends PropertyNamingStrategy {

    @Override
    public String nameForField(MapperConfig config, AnnotatedField field, String defaultName) {
        if (defaultName.equalsIgnoreCase("latitude")) {
            return "lat";
        } else if (defaultName.equalsIgnoreCase("longitude")) {
            return "lon";
        }
        return super.nameForField(config, field, defaultName);
    }

    @Override
    public String nameForGetterMethod(MapperConfig config, AnnotatedMethod method, String defaultName) {
        if (defaultName.equalsIgnoreCase("latitude")) {
            return "lat";
        } else if (defaultName.equalsIgnoreCase("longitude")) {
            return "lon";
        }
        return super.nameForGetterMethod(config, method, defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig config, AnnotatedMethod method, String defaultName) {
        if (defaultName.equalsIgnoreCase("latitude")) {
            return "lat";
        } else if (defaultName.equalsIgnoreCase("longitude")) {
            return "lon";
        }
        return super.nameForSetterMethod(config, method, defaultName);
    }

}
