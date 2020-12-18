package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.enums.MarketCode;

@EqualsAndHashCode(callSuper = false)
@JsonObject
@Data
@Slf4j
public class AccountDetailsResponse extends AbstractResponse {

    private static final Pattern EXTRACT_ACCOUNT_OWNERS_PATTERN = Pattern.compile("- (.*)");
    private static final int SCALE = 8;

    private AccountInterestDetailsEntity accountInterestDetails;
    private List<String> accountOwners = Collections.emptyList();
    private BigDecimal feeAmount;
    private String feeCurrency;
    private String accountType;
    private String iban;
    private String bic;

    public Double getInterestRate() {
        /* Scale in division is set to 8 places, because in banking we should provide 6 digits after
        the decimal, and here we also divide by 100, although in Danske Bank's response interest
        rates are usually rounded to 3 digits after the decimal.
        Divide by 100 because we should return decimal number, not percent.
        */
        try {
            // Wiski please remove unnecessary logging after getting proper logs
            return getInterestDetailEntities().stream()
                    .peek(
                            interestDetail ->
                                    log.info(
                                            "Danske interestDetail with text: [{}] and rate [{}] 0",
                                            interestDetail.getText(),
                                            getInterestRateDescriptionForLog(interestDetail)))
                    .map(interestDetail -> Double.parseDouble(interestDetail.getRate()))
                    .filter(rate -> rate > 0)
                    .findFirst()
                    .map(
                            rate ->
                                    BigDecimal.valueOf(rate)
                                            .divide(
                                                    BigDecimal.valueOf(100),
                                                    SCALE,
                                                    RoundingMode.HALF_UP)
                                            .doubleValue())
                    .orElse(null);
        } catch (NumberFormatException | ArithmeticException | NullPointerException e) {
            log.warn("Couldn't parse interest rate", e);
            return null;
        }
    }

    private List<InterestDetailEntity> getInterestDetailEntities() {
        return Optional.ofNullable(accountInterestDetails)
                .map(
                        accountInterestDetailsEntity -> {
                            log.info(
                                    "Danske Bank interestDetails with size: {}",
                                    accountInterestDetails.getInterestDetails().size());
                            return accountInterestDetails.getInterestDetails();
                        })
                .orElse(Collections.emptyList());
    }

    private String getInterestRateDescriptionForLog(InterestDetailEntity interestDetailEntity) {
        try {
            double rate = Double.parseDouble(interestDetailEntity.getRate());
            if (rate > 0) {
                return "higher than";
            } else if (rate == 0) {
                return "equal to";
            }
            return "lower than";
        } catch (NumberFormatException e) {
            log.warn("Couldn't log interest rate description", e);
            return null;
        }
    }

    public List<String> getAccountOwners(String marketCode) {
        // We don't know what is returned in Finland
        if (marketCode.equals(MarketCode.DK.name())) {
            return accountOwners.stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
        } else if (marketCode.equals(MarketCode.NO.name())
                || marketCode.equals(MarketCode.SE.name())) {
            return extractAccountOwners();
        }
        return Collections.emptyList();
    }

    private List<String> extractAccountOwners() {
        try {
            return accountOwners.stream()
                    .filter(StringUtils::isNotBlank)
                    .map(
                            accountOwner -> {
                                Matcher matcher =
                                        EXTRACT_ACCOUNT_OWNERS_PATTERN.matcher(accountOwner);
                                if (matcher.find()) {
                                    return matcher.group(1);
                                }
                                throw new IllegalStateException(
                                        "Found accountOwner that couldn't be extracted");
                            })
                    .collect(Collectors.toList());
        } catch (IllegalStateException e) {
            log.warn(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
