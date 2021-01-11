package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.storage;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class BankdataStorage {

    private final Storage storage;

    public BankdataStorage(Storage storage) {
        this.storage = storage;
    }

    public Optional<String> getNemidInstallId() {
        return Optional.ofNullable(
                Strings.emptyToNull(storage.get(NemIdConstants.Storage.NEMID_INSTALL_ID)));
    }
}
