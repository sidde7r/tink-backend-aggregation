package se.tink.backend.system.workers.processor.formatting;

import se.tink.backend.utils.StringUtils;

public final class BasicDescriptionFormatter implements DescriptionFormatter {

    @Override
    public String clean(String description) {
        return StringUtils.trimToNull(description);
    }

    @Override
    public String extrapolate(String description) {
        return description;
    }
}
