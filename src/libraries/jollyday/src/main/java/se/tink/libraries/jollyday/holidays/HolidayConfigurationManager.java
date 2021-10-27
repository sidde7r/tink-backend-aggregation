package se.tink.libraries.jollyday.holidays;

import de.jollyday.config.Configuration;
import java.util.HashMap;
import java.util.Map;
import se.tink.libraries.jollyday.CountryCode;

public class HolidayConfigurationManager {

    // To add configuration for new countries check:
    // https://github.com/svendiedrichsen/jollyday/tree/master/src/main/resources/holidays
    // and create configuration class accordingly

    private static HolidayConfigurationManager instance = new HolidayConfigurationManager();
    private final Map<CountryCode, Configuration> configurationMapForCountries;

    private HolidayConfigurationManager() {
        this.configurationMapForCountries = new HashMap<>();
        this.configurationMapForCountries.put(
                CountryCode.SE, new SwedenHolidayConfigurationCreator().create());
        this.configurationMapForCountries.put(
                CountryCode.BE, new BelgiumHolidayConfigurationCreator().create());
        this.configurationMapForCountries.put(
                CountryCode.FR, new FranceHolidayConfigurationCreator().create());
        this.configurationMapForCountries.put(
                CountryCode.UK, new UkHolidayConfigurationCreator().create());
        this.configurationMapForCountries.put(
                CountryCode.PT, new PortugalHolidayConfigurationCreator().create());
    }

    public static HolidayConfigurationManager getInstance() {
        if (instance == null) {
            instance = new HolidayConfigurationManager();
        }
        return instance;
    }

    public Configuration getConfiguration(CountryCode countryCode) {
        if (!configurationMapForCountries.containsKey(countryCode)) {
            throw new UnsupportedOperationException(
                    String.format(
                            "Does not have a holiday configuration for %s, please add it.",
                            countryCode));
        }
        return configurationMapForCountries.get(countryCode);
    }
}
