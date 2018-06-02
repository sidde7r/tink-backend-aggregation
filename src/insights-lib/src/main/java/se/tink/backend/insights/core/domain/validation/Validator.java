package se.tink.backend.insights.core.domain.validation;

import com.google.common.base.Strings;
import se.tink.backend.insights.core.domain.model.HasExternalId;
import se.tink.backend.insights.core.domain.model.Insight;

public class Validator {

    public static boolean externalIdNotNullOrEmpty(Insight insight){
        if (insight instanceof HasExternalId) {
            String id = ((HasExternalId) insight).getExternalId();
            return !Strings.isNullOrEmpty(id);
        }
        return false;
    }
}
