package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISAL;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISPA;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;

@Data
@Accessors(chain = true)
public class FinTsAccountInformation {
    @Getter private HIUPD basicInfo;
    private HISPA.Detail sepaDetails;
    private HISAL balance;

    public FinTsAccountInformation(HIUPD basicInfo) {
        this.basicInfo = basicInfo;
    }
}
