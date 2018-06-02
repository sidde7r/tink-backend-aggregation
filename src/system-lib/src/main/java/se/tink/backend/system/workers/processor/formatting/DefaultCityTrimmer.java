package se.tink.backend.system.workers.processor.formatting;

import se.tink.backend.utils.CityDescriptionTrimmer;

public class DefaultCityTrimmer implements CityTrimmer {
    private final CityDescriptionTrimmer trimmer;

    public DefaultCityTrimmer(CityDescriptionTrimmer trimmer) {
        this.trimmer = trimmer;
    }

    @Override
    public String trim(String description) {
        return trimmer.trim(description);
    }
}
