package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.Balance;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.BasicAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.SepaDetails;

@Data
@Accessors(chain = true)
public class FinTsAccountInformation {
    private BasicAccountInformation basicInfo;
    private SepaDetails.Detail sepaDetails;
    private Balance balance;

    public FinTsAccountInformation(BasicAccountInformation basicInfo) {
        this.basicInfo = basicInfo;
    }
}
