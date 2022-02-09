package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.List;
import se.tink.libraries.transfer.rpc.Transfer;

public class BulkTransferRequestModule extends AbstractModule {
    private final List<Transfer> bulkTransfer;

    public BulkTransferRequestModule(List<Transfer> bulkTransfer) {
        this.bulkTransfer = bulkTransfer;
    }

    @Provides
    @Singleton
    private List<Transfer> provideBulkTransfer() {
        return bulkTransfer;
    }
}
