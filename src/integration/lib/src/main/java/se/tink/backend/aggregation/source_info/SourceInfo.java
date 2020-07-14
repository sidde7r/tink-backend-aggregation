package se.tink.backend.aggregation.source_info;

import lombok.Builder;
import lombok.Getter;

/**
 * Information that should contain the original data (that bank has sent over in payload). This
 * information is to be send to to Google's Bigtable for analysis/link-bank. As an example: it would
 * be possible to get an insight at which accounts in particular need further attention when it
 * comes to e.g. Account Type or Account Capabilities determination.
 */
@Builder
@Getter
public class SourceInfo {
    private String bankProductCode;
    private String bankProductName;
    private String bankAccountType;

    @Override
    public String toString() {
        return "bankProductCode='"
                + bankProductCode
                + '\''
                + "; bankProductName='"
                + bankProductName
                + '\''
                + "; bankAccountType='"
                + bankAccountType;
    }
}
