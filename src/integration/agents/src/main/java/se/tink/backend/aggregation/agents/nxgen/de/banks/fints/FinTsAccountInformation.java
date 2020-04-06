package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISAL;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISPA;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;

@Data
@Accessors(chain = true)
public class FinTsAccountInformation {
    private HIUPD basicInfo;
    private HISPA.Detail sepaDetails;
    private HISAL balance;
    private AccountTypes accountType;

    public FinTsAccountInformation(HIUPD basicInfo) {
        this.basicInfo = basicInfo;
    }

    public FinTsAccountInformation(HIUPD basicInfo, AccountTypes accountType) {
        this.basicInfo = basicInfo;
        this.accountType = accountType;
    }
}
