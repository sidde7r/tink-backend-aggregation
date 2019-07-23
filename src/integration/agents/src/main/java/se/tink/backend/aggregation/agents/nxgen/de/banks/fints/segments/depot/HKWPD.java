package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.depot;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsBankIdentifier.countryAlphaToNumeric;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.DataGroup;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.accounts.SEPAAccount;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils.FinTsEscape;

public class HKWPD extends FinTsSegment {

    public HKWPD(int segmentNumber, SEPAAccount account, String touchDown) {
        super(segmentNumber, false);

        DataGroup accountGroup = new DataGroup();
        accountGroup.addElement(account.getAccountNo());
        accountGroup.addElement(account.getSubAccount());
        String bic = account.getBic();
        accountGroup.addElement(
                countryAlphaToNumeric(
                        (bic == null || "".equals(bic)) ? "DE" : bic.substring(4, 6)));
        accountGroup.addElement(account.getBlz());
        addDataGroup(accountGroup);
        addDataGroup(touchDown == null ? "" : FinTsEscape.escapeDataElement(touchDown));
    }

    @Override
    public int getVersion() {
        return 5;
    }

    @Override
    public String getType() {
        return FinTsConstants.Segments.HKWPD.name();
    }
}
