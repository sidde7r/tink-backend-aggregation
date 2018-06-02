package se.tink.backend.export.client;

import se.tink.backend.export.GdprExportService;

public interface GdprExportServiceFactory {
    String SERVICE_NAME = "data-export";

    GdprExportService getGdprExportService();

}
