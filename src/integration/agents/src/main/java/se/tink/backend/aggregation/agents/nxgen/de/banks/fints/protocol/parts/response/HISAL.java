package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawGroup;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Accessors(chain = true)
@NoArgsConstructor
@Getter
@Setter
public class HISAL extends BaseResponsePart {
    private String iban;
    private String bic;
    private String accountNumber;
    private String subAccountNumber;
    private String countryCode;
    private String blz;

    private String currency;
    private BigDecimal firstBalanceValue;
    private BigDecimal secondBalanceValue;

    HISAL(RawSegment rawSegment) {
        super(rawSegment);

        currency = rawSegment.getGroup(3).getString(0);
        RawGroup group = rawSegment.getGroup(4);
        firstBalanceValue = balanceWithSign(group.getString(0), group.getDecimal(1));
        group = rawSegment.getGroup(5);
        secondBalanceValue =
                group.isEmpty() ? null : balanceWithSign(group.getString(0), group.getDecimal(1));

        switch (getSegmentVersion()) {
            case 5:
            case 6:
                accountNumber = rawSegment.getGroup(1).getString(0);
                subAccountNumber = rawSegment.getGroup(1).getString(1);
                countryCode = rawSegment.getGroup(1).getString(2);
                blz = rawSegment.getGroup(1).getString(3);
                break;
            case 7:
                iban = rawSegment.getGroup(1).getString(0);
                bic = rawSegment.getGroup(1).getString(1);
                accountNumber = rawSegment.getGroup(1).getString(2);
                subAccountNumber = rawSegment.getGroup(1).getString(3);
                countryCode = rawSegment.getGroup(1).getString(4);
                blz = rawSegment.getGroup(1).getString(5);
                break;
            default:
                break;
        }
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Arrays.asList(5, 6, 7);
    }

    private BigDecimal balanceWithSign(String creditOrDebitChar, BigDecimal amount) {
        Preconditions.checkArgument(creditOrDebitChar != null);

        switch (creditOrDebitChar) {
            case "C":
                return amount;
            case "D":
                return amount.negate();
            default:
                throw new IllegalArgumentException("Unknown balance type: " + creditOrDebitChar);
        }
    }
}
