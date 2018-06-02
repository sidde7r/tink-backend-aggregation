package se.tink.backend.system.document.mapper;

import se.tink.backend.common.config.BackOfficeConfiguration;
import se.tink.backend.system.document.core.DocumentModeratorDispatchDetails;

public class BackOfficeConfigurationToDocumentModeratorDetailsMapper {
    public static DocumentModeratorDispatchDetails translate(BackOfficeConfiguration configuration) {
        return new DocumentModeratorDispatchDetails(configuration.getToAddress(), configuration.getFromAddress(),
                configuration.getFromName());
    }
}
