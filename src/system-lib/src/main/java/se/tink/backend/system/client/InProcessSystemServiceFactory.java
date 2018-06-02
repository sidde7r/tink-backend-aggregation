package se.tink.backend.system.client;

import com.google.inject.Inject;
import se.tink.backend.system.api.CronService;
import se.tink.backend.system.api.ProcessService;
import se.tink.backend.system.api.UpdateService;
import se.tink.backend.system.transports.NotificationTransport;

public class InProcessSystemServiceFactory implements SystemServiceFactory {

    private final NotificationTransport notificationGatewayService;

    private UpdateService updateService;
    private ProcessService processService;
    private CronService cronService;

    @Inject
    InProcessSystemServiceFactory(NotificationTransport notificationGatewayService) {
        this.notificationGatewayService = notificationGatewayService;
    }

    @Override
    public CronService getCronService() {
        return cronService;
    }

    void setCronService(CronService cronService) {
        this.cronService = cronService;
    }

    @Override
    public UpdateService getUpdateService() {
        return updateService;
    }

    void setUpdateService(UpdateService updateService) {
        this.updateService = updateService;
    }

    public NotificationTransport getNotificationGatewayService() {
        return notificationGatewayService;
    }

    @Override
    public ProcessService getProcessService() {
        return processService;
    }

    void setProcessService(ProcessService processService) {
        this.processService = processService;
    }
}
