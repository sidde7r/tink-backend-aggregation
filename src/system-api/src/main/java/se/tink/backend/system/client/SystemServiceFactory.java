package se.tink.backend.system.client;

import se.tink.backend.system.api.ProcessService;
import se.tink.backend.system.api.UpdateService;

public interface SystemServiceFactory {
    String SERVICE_NAME = "system";

    UpdateService getUpdateService();

    ProcessService getProcessService();
}
