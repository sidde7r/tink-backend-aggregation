package se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.module;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.libraries.transfer.rpc.Transfer;

public final class TransferRequestModule extends AbstractModule {

    private final Transfer transfer;

    public TransferRequestModule(Transfer transfer) {
        this.transfer = transfer;
    }

    @Provides
    @Singleton
    private Transfer provideTransfer() {
        return transfer;
    }
}
