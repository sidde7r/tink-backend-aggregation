package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.entities.DetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.entities.LoanAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.entities.PaymentDetailEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities.LinkEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.loan.util.InterestRateConverter;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class HandelsbankenNOLoanAccountFetcher implements AccountFetcher<LoanAccount> {

    private static final String REPAYMENT_PLAN_LINK_KEY = "repayment_plan";
    private static final String NOK_CURRENCY_CODE = "NOK";
    private final HandelsbankenNOApiClient handelsbankenNOApiClient;

    public HandelsbankenNOLoanAccountFetcher(HandelsbankenNOApiClient handelsbankenNOApiClient) {
        this.handelsbankenNOApiClient = handelsbankenNOApiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return handelsbankenNOApiClient.fetchLoans().getLoanAccountEntities().stream()
                .peek(
                        loanAccountEntity ->
                                log.info(
                                        "LoanAccountEntity of type [{}] and with accountDescription [{}]",
                                        loanAccountEntity.getType(),
                                        loanAccountEntity.getAccountDescription()))
                .map(this::convertToTinkLoanAccount)
                .collect(Collectors.toList());
    }

    private LoanAccount convertToTinkLoanAccount(LoanAccountEntity loanEntity) {
        LoanDetailsResponse loanDetailsResponse =
                getLoanDetailsResponseIfPathWasReturned(loanEntity);
        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(getLoanType(loanEntity.getAccountDescription()))
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                loanEntity.getBalance(), NOK_CURRENCY_CODE))
                                .withInterestRate(
                                        InterestRateConverter.toDecimalValue(
                                                loanDetailsResponse.getNominalInterestRate(), 6))
                                .setMonthlyAmortization(
                                        ExactCurrencyAmount.of(
                                                getInstalment(loanDetailsResponse),
                                                NOK_CURRENCY_CODE))
                                .setInitialBalance(
                                        ExactCurrencyAmount.inNOK(
                                                loanDetailsResponse.getOriginalLoanAmount()))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(loanEntity.getId())
                                .withAccountNumber(loanEntity.getAccountNumber())
                                .withAccountName(loanEntity.getAccountDescription())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.NO, loanEntity.getId()))
                                .build())
                .build();
    }

    private LoanDetailsResponse getLoanDetailsResponseIfPathWasReturned(
            LoanAccountEntity loanEntity) {

        Optional<String> link =
                Optional.ofNullable(loanEntity.getLinks())
                        .map(links -> links.get(REPAYMENT_PLAN_LINK_KEY))
                        .map(LinkEntity::getHref);

        if (!link.isPresent()) {
            log.warn(
                    "LoanEntity doesn't contain repayment_plan link!, links: [{}]",
                    loanEntity.getLinks());
            LoanDetailsResponse loanDetailsResponse = new LoanDetailsResponse();
            loanDetailsResponse.setNominalInterestRate(0);
            loanDetailsResponse.setOriginalLoanAmount(0);
            return loanDetailsResponse;
        }

        return handelsbankenNOApiClient.fetchLoanDetails(
                loanEntity.getLinks().get(REPAYMENT_PLAN_LINK_KEY).getHref());
    }

    private LoanDetails.Type getLoanType(String description) {
        return HandelsbankenNOConstants.LoanType.LOANS_DESCRIPTIONS.entrySet().stream()
                .filter(entry -> StringUtils.containsIgnoreCase(description, entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(LoanDetails.Type.DERIVE_FROM_NAME);
    }

    private BigDecimal getInstalment(LoanDetailsResponse loanDetailsResponse) {
        Optional<BigDecimal> optionalInstalment =
                Optional.ofNullable(loanDetailsResponse.getPaymentDetail())
                        .map(
                                paymentDetailEntities -> {
                                    if (paymentDetailEntities.isEmpty()) {
                                        return new PaymentDetailEntity();
                                    }
                                    return paymentDetailEntities.get(0);
                                })
                        .map(PaymentDetailEntity::getDetails)
                        .map(
                                detailsEntities -> {
                                    if (detailsEntities.isEmpty()) {
                                        return new DetailsEntity();
                                    }
                                    return detailsEntities.get(0);
                                })
                        .map(DetailsEntity::getInstalment);

        if (!optionalInstalment.isPresent()) {
            log.warn("LoanDetailsEntity doesn't contain instalment!");
            return BigDecimal.ZERO;
        }
        return loanDetailsResponse.getPaymentDetail().get(0).getDetails().get(0).getInstalment();
    }
}
