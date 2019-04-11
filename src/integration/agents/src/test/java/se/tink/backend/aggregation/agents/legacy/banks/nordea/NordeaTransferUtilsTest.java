package se.tink.backend.aggregation.agents.banks.nordea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.Optional;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.CreatePaymentIn;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.CreatePaymentOut;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.PaymentEntity;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.TransferRequest;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.TransferResponse;
import se.tink.libraries.account.AccountIdentifier;

@RunWith(Enclosed.class)
public class NordeaTransferUtilsTest {
    public static class GetSingleMatchingPaymentEntity {
        @Test
        public void presentIfSame() throws IOException {
            TransferRequest paymentRequest = stubPaymentRequestDefault();
            TransferResponse paymentResponse = stubPaymentResponseDefault();
            PaymentEntity paymentEntity = stubPaymentEntityDefault();

            ImmutableList<PaymentEntity> paymentEntities = ImmutableList.of(paymentEntity);

            Optional<PaymentEntity> singleMatchingPaymentEntity =
                    NordeaTransferUtils.getSingleMatchingPaymentEntity(
                            paymentEntities, paymentRequest, paymentResponse, null);

            assertThat(singleMatchingPaymentEntity.isPresent()).isTrue();
        }

        @Test
        public void absentIfMultipleOfSame() throws IOException {
            TransferRequest paymentRequest = stubPaymentRequestDefault();
            TransferResponse paymentResponse = stubPaymentResponseDefault();
            PaymentEntity paymentEntity = stubPaymentEntityDefault();

            ImmutableList<PaymentEntity> paymentEntities =
                    ImmutableList.of(paymentEntity, paymentEntity);

            Optional<PaymentEntity> singleMatchingPaymentEntity =
                    NordeaTransferUtils.getSingleMatchingPaymentEntity(
                            paymentEntities, paymentRequest, paymentResponse, null);

            assertThat(singleMatchingPaymentEntity.isPresent()).isFalse();
        }

        @Test
        public void expectedAndPresentIfOnlyOneOfMultipleMatches() throws IOException {
            TransferRequest paymentRequest = stubPaymentRequestDefault();
            TransferResponse paymentResponse = stubPaymentResponseDefault();
            PaymentEntity paymentEntity = stubPaymentEntityDefault();
            PaymentEntity differentPaymentEntity = stubPaymentEntityThatDiffersCompletely();

            ImmutableList<PaymentEntity> paymentEntities =
                    ImmutableList.of(paymentEntity, differentPaymentEntity);

            Optional<PaymentEntity> singleMatchingPaymentEntity =
                    NordeaTransferUtils.getSingleMatchingPaymentEntity(
                            paymentEntities, paymentRequest, paymentResponse, null);

            assertThat(singleMatchingPaymentEntity.isPresent()).isTrue();

            PaymentEntity selectedPayment = singleMatchingPaymentEntity.get();
            assertThat(selectedPayment).isSameAs(paymentEntity);
            assertThat(selectedPayment).isNotSameAs(differentPaymentEntity);
        }

        @Test
        public void absentIfAmountDiffers() throws IOException {
            TransferRequest paymentRequest = stubPaymentRequestDefault();
            TransferResponse paymentResponse = stubPaymentResponseDefault();
            PaymentEntity paymentEntity = stubPaymentEntityDefault();

            paymentRequest.getCreatePaymentIn().setAmount("1.02");
            paymentEntity.setAmount("1.03");
            ImmutableList<PaymentEntity> paymentEntities = ImmutableList.of(paymentEntity);

            Optional<PaymentEntity> singleMatchingPaymentEntity =
                    NordeaTransferUtils.getSingleMatchingPaymentEntity(
                            paymentEntities, paymentRequest, paymentResponse, null);

            assertThat(singleMatchingPaymentEntity.isPresent()).isFalse();
        }

        @Test
        public void absentIfCurrencyDiffers() throws IOException {
            TransferRequest paymentRequest = stubPaymentRequestDefault();
            TransferResponse paymentResponse = stubPaymentResponseDefault();
            PaymentEntity paymentEntity = stubPaymentEntityDefault();

            paymentRequest.getCreatePaymentIn().setCurrency("SOME CURRENCY");
            paymentEntity.setCurrency("OTHER CURRENCY");
            ImmutableList<PaymentEntity> paymentEntities = ImmutableList.of(paymentEntity);

            Optional<PaymentEntity> singleMatchingPaymentEntity =
                    NordeaTransferUtils.getSingleMatchingPaymentEntity(
                            paymentEntities, paymentRequest, paymentResponse, null);

            assertThat(singleMatchingPaymentEntity.isPresent()).isFalse();
        }

        @Test
        public void absentIfPaymentSubtypeDiffers() throws IOException {
            TransferRequest paymentRequest = stubPaymentRequestDefault();
            TransferResponse paymentResponse = stubPaymentResponseDefault();
            PaymentEntity paymentEntity = stubPaymentEntityDefault();

            paymentRequest.getCreatePaymentIn().setPaymentSubType("SOME SUBTYPE");
            paymentEntity.setPaymentSubType("OTHER SUBTYPE");
            ImmutableList<PaymentEntity> paymentEntities = ImmutableList.of(paymentEntity);

            Optional<PaymentEntity> singleMatchingPaymentEntity =
                    NordeaTransferUtils.getSingleMatchingPaymentEntity(
                            paymentEntities, paymentRequest, paymentResponse, null);

            assertThat(singleMatchingPaymentEntity.isPresent()).isFalse();
        }

        @Test
        public void absentIfToAccountIdDiffers() throws IOException {
            TransferRequest paymentRequest = stubPaymentRequestDefault();
            TransferResponse paymentResponse = stubPaymentResponseDefault();
            PaymentEntity paymentEntity = stubPaymentEntityDefault();

            paymentRequest.getCreatePaymentIn().setToAccountId("SOME TO");
            paymentEntity.setToAccountId("OTHER TO");
            ImmutableList<PaymentEntity> paymentEntities = ImmutableList.of(paymentEntity);

            Optional<PaymentEntity> singleMatchingPaymentEntity =
                    NordeaTransferUtils.getSingleMatchingPaymentEntity(
                            paymentEntities, paymentRequest, paymentResponse, null);

            assertThat(singleMatchingPaymentEntity.isPresent()).isFalse();
        }

        @Test
        public void absentIfDateDiffersMoreThanOneSecond() throws IOException {
            TransferRequest paymentRequest = stubPaymentRequestDefault();
            TransferResponse paymentResponse = stubPaymentResponseDefault();
            PaymentEntity paymentEntity = stubPaymentEntityDefault();

            paymentResponse.getCreatePaymentOut().setPaymentDate("2016-03-10T12:00:00.023+01:00");
            paymentEntity.setPaymentDate("2016-03-10T12:00:01.023+01:00");
            ImmutableList<PaymentEntity> paymentEntities = ImmutableList.of(paymentEntity);

            Optional<PaymentEntity> singleMatchingPaymentEntity =
                    NordeaTransferUtils.getSingleMatchingPaymentEntity(
                            paymentEntities, paymentRequest, paymentResponse, null);

            assertThat(singleMatchingPaymentEntity.isPresent()).isFalse();
        }

        /**
         * Reason it works like this is since Nordea has different payment dates in MS when POST a
         * new payment and when GET list of payments that are unsigned
         */
        @Test
        public void presentIfDateDiffersLessThanOneSecond() throws IOException {
            TransferRequest paymentRequest = stubPaymentRequestDefault();
            TransferResponse paymentResponse = stubPaymentResponseDefault();
            PaymentEntity paymentEntity = stubPaymentEntityDefault();

            paymentResponse.getCreatePaymentOut().setPaymentDate("2016-03-10T12:00:00.023+01:00");
            paymentEntity.setPaymentDate("2016-03-10T12:00:00.112+01:00");
            ImmutableList<PaymentEntity> paymentEntities = ImmutableList.of(paymentEntity);

            Optional<PaymentEntity> singleMatchingPaymentEntity =
                    NordeaTransferUtils.getSingleMatchingPaymentEntity(
                            paymentEntities, paymentRequest, paymentResponse, null);

            assertThat(singleMatchingPaymentEntity.isPresent()).isTrue();
        }

        /**
         * Nordea does not return same account identifier for BeneficiaryEntity as for
         * PaymentEntity, so we need to fallback on our Transfer.getDestination() identifier to e.g.
         * pad the recipient account number with 0's for Handelsbanken
         */
        @Test
        public void presentIfFormattedAccountIdentifierMatches() throws IOException {
            TransferRequest paymentRequest = stubPaymentRequestDefault();
            TransferResponse paymentResponse = stubPaymentResponseDefault();
            PaymentEntity paymentEntity = stubPaymentEntityDefault();

            paymentRequest.getCreatePaymentIn().setToAccountId("6769392752158");
            paymentEntity.setToAccountId("67690392752158");
            AccountIdentifier accountIdentifier =
                    AccountIdentifier.create(AccountIdentifier.Type.SE, "6769392752158");

            ImmutableList<PaymentEntity> paymentEntities = ImmutableList.of(paymentEntity);

            Optional<PaymentEntity> singleMatchingPaymentEntity =
                    NordeaTransferUtils.getSingleMatchingPaymentEntity(
                            paymentEntities, paymentRequest, paymentResponse, accountIdentifier);

            assertThat(singleMatchingPaymentEntity.isPresent()).isTrue();
        }

        /** Oposite of above to check that we don't accept any formatted identifier */
        @Test
        public void absentIfFormattedAccountIdentifierDiffers() throws IOException {
            TransferRequest paymentRequest = stubPaymentRequestDefault();
            TransferResponse paymentResponse = stubPaymentResponseDefault();
            PaymentEntity paymentEntity = stubPaymentEntityDefault();

            paymentRequest.getCreatePaymentIn().setToAccountId("6769392752158");
            paymentEntity.setToAccountId("67690392752158");
            AccountIdentifier accountIdentifier =
                    AccountIdentifier.create(AccountIdentifier.Type.SE, "676900392752158");

            ImmutableList<PaymentEntity> paymentEntities = ImmutableList.of(paymentEntity);

            Optional<PaymentEntity> singleMatchingPaymentEntity =
                    NordeaTransferUtils.getSingleMatchingPaymentEntity(
                            paymentEntities, paymentRequest, paymentResponse, accountIdentifier);

            assertThat(singleMatchingPaymentEntity.isPresent()).isFalse();
        }
    }

    private static TransferRequest stubPaymentRequestDefault() throws IOException {
        return stubPaymentRequest(
                "11.22", "BENEFICIARY_NAME", "CURRENCY", "PAYMENT_SUBTYPE", "TO_ACCOUNT_ID");
    }

    private static TransferResponse stubPaymentResponseDefault() {
        return stubPaymentResponse("2016-03-10T12:00:00.023+01:00");
    }

    private static PaymentEntity stubPaymentEntityDefault() {
        return stubPaymentEntity(
                "11.22",
                "BENEFICIARY_NAME",
                "CURRENCY",
                "2016-03-10T12:00:00.023+01:00",
                "PAYMENT_SUBTYPE",
                "TO_ACCOUNT_ID");
    }

    private static PaymentEntity stubPaymentEntityThatDiffersCompletely() {
        return stubPaymentEntity(
                "11.23",
                "BENEFICIARY_NAME2",
                "CURRENCY2",
                "2016-03-10T12:00:01.023+01:00",
                "PAYMENT_SUBTYPE2",
                "TO_ACCOUNT_ID2");
    }

    private static TransferRequest stubPaymentRequest(
            String amount,
            String beneficiaryName,
            String currency,
            String paymentSubType,
            String toAccountId) {
        TransferRequest stub = mock(TransferRequest.class);

        CreatePaymentIn createPaymentIn = new CreatePaymentIn();
        createPaymentIn.setAmount(amount);
        createPaymentIn.setBeneficiaryName(beneficiaryName);
        createPaymentIn.setCurrency(currency);
        createPaymentIn.setPaymentSubType(paymentSubType);
        createPaymentIn.setToAccountId(toAccountId);

        when(stub.getCreatePaymentIn()).thenReturn(createPaymentIn);

        return stub;
    }

    private static TransferResponse stubPaymentResponse(String paymentDate) {
        TransferResponse stub = mock(TransferResponse.class);

        CreatePaymentOut createPaymentOut = new CreatePaymentOut();
        createPaymentOut.setPaymentDate(paymentDate);

        when(stub.getCreatePaymentOut()).thenReturn(createPaymentOut);

        return stub;
    }

    private static PaymentEntity stubPaymentEntity(
            String amount,
            String beneficiaryName,
            String currency,
            String paymentDate,
            String paymentSubtype,
            String toAccountId) {
        PaymentEntity stub = new PaymentEntity();

        stub.setAmount(amount);
        stub.setBeneficiaryName(beneficiaryName);
        stub.setCurrency(currency);
        stub.setPaymentDate(paymentDate);
        stub.setPaymentSubType(paymentSubtype);
        stub.setToAccountId(toAccountId);

        return stub;
    }
}
