package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsPaymentInitiationRequest;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SibsUtilsTest {

    private static final String EXPECTED_REAL_SCENARIO_CONSENT_DIGEST =
            "qBQR5yVgrJPoj3VyEXRx95BshI3gCDrmClp+DxvLvHU=";

    private static final String EXPECTED_REAL_SCENARIO_PAYMENT_DIGEST =
            "eWyqmwv30yxXMtSvfbbJhDkvaSLd+m37lh0Jz12tcDs=";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(SibsConstants.Formats.CONSENT_BODY_DATE_FORMAT);

    /*
    Log from real example:
        Digest: SHA-256=eWyqmwv30yxXMtSvfbbJhDkvaSLd+m37lh0Jz12tcDs=
    { "debtorAccount":{ "iban":"PT50001800033415092002025" }, "instructedAmount":{ "currency":"EUR", "content":"1.00" }, "creditorAccount":{ "iban":"PT50001800034257091102046" }, "creditorName":"José Neves" }
     */
    @Test
    public void shouldCalculateDigestSameAsInRealPaymentRequestScenario() {
        SibsPaymentInitiationRequest sibsPaymentInitiationRequest =
                getSibsPaymentInitiationRequest();

        String signature = SibsUtils.getDigest(sibsPaymentInitiationRequest);

        Assertions.assertThat(signature).isEqualTo(EXPECTED_REAL_SCENARIO_PAYMENT_DIGEST);
    }

    /*
    Log from real example:
        Digest: SHA-256=qBQR5yVgrJPoj3VyEXRx95BshI3gCDrmClp+DxvLvHU=
        {"access":{"allPsd2":"all-accounts"},"recurringIndicator":true,"validUntil":"2019-12-25T05:27:21","frequencyPerDay":4,"combinedServiceIndicator":false}
     */
    @Test
    public void shouldCalculateDigestSameAsInRealConsentRequestScenario() {
        String signature = SibsUtils.getDigest(getConsentRequest());

        Assertions.assertThat(signature).isEqualTo(EXPECTED_REAL_SCENARIO_CONSENT_DIGEST);
    }

    @Test
    public void shouldCreateDateStringForConsentsValidFor90Days() {
        String date = SibsUtils.get90DaysValidConsentStringDate(new ConstantLocalDateTimeSource());

        String expectedDate =
                DATE_FORMATTER.format(LocalDate.of(1992, 7, 9).atStartOfDay(ZoneOffset.UTC));

        Assertions.assertThat(date).isEqualTo(expectedDate);
    }

    @Test
    public void shouldReturnNullWhenLocalDateIsNullConvertStringToLocalDate() {
        Assertions.assertThat(SibsUtils.convertStringToLocalDate(null)).isNull();
    }

    @Test
    public void shouldReturnNullWhenLocalDateIsEmptyConvertStringToLocalDate() {
        Assertions.assertThat(SibsUtils.convertStringToLocalDate("")).isNull();
    }

    @Test
    public void shouldReturnNullWhenLocalDateIsNullConvertLocalDateToString() {
        Assertions.assertThat(SibsUtils.convertLocalDateToString(null)).isNull();
    }

    @Test
    public void shouldReturnUuidWithoutDashes() {
        Assertions.assertThat(SibsUtils.getRequestId(new MockRandomValueGenerator()))
                .isEqualTo("00000000000040000000000000000000");
    }

    private ConsentRequest getConsentRequest() {
        LocalDateTime now = LocalDateTime.of(2019, 12, 25, 5, 27, 21);
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(now);
        return new ConsentRequest(
                new ConsentAccessEntity(SibsConstants.FormValues.ALL_ACCOUNTS),
                true,
                date,
                SibsConstants.FormValues.FREQUENCY_PER_DAY,
                false);
    }

    private SibsPaymentInitiationRequest getSibsPaymentInitiationRequest() {
        SibsAccountReferenceEntity debtor = new SibsAccountReferenceEntity();
        debtor.setIban("PT50001800033415092002025");
        SibsAccountReferenceEntity creditor = new SibsAccountReferenceEntity();
        creditor.setIban("PT50001800034257091102046");

        return new SibsPaymentInitiationRequest.Builder()
                .withCreditorAccount(creditor)
                .withDebtorAccount(debtor)
                .withInstructedAmount(
                        SibsAmountEntity.of(new ExactCurrencyAmount(new BigDecimal("1.0"), "EUR")))
                .withCreditorName("José Neves")
                .build();
    }
}
