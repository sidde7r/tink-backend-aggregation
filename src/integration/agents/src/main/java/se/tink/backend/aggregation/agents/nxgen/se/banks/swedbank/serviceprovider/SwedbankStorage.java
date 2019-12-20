package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfileHandler;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class SwedbankStorage extends Storage {

    public SwedbankStorage() {
        super();
    }

    public BankProfileHandler getBankProfileHandler() {
        return this.get(
                        SwedbankBaseConstants.StorageKey.BANK_PROFILE_HANDLER,
                        BankProfileHandler.class)
                .orElseThrow(IllegalStateException::new);
    }

    public void setBankProfileHandler(BankProfileHandler bankProfileHandler) {
        this.put(SwedbankBaseConstants.StorageKey.BANK_PROFILE_HANDLER, bankProfileHandler);
    }
}
