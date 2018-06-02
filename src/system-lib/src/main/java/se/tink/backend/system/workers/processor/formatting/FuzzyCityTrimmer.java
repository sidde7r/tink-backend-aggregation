package se.tink.backend.system.workers.processor.formatting;

import se.tink.backend.utils.CityDescriptionTrimmer;

public class FuzzyCityTrimmer implements CityTrimmer {
    private CityDescriptionTrimmer trimmer;

    public FuzzyCityTrimmer(CityDescriptionTrimmer trimmer) {
        this.trimmer = trimmer;
    }

    @Override
    public String trim(String description) {
        return trimmer.trimWithFuzzyFallback(description);
    }
}
