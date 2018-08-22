package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient.Signable;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.HandelsbankenSEPaymentAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public class TransferSpecificationResponse extends BaseResponse implements ExecutorExceptionResolver.Messageable {

    private String status;
    private HandelsbankenSEPaymentAccount fromAccount;
    private HandelsbankenSEPaymentAccount toAccount;
    private HandelsbankenAmount amount;

    @Override
    public String getStatus() {
        return status;
    }

    public Signable toSignable(ExecutorExceptionResolver exceptionResolver) {
        return () -> searchLink(HandelsbankenConstants.URLS.Links.SIGNATURE)
                        .orElseThrow(() -> exceptionResolver.asException(this));
    }

    public HandelsbankenSEPaymentAccount getFromAccount() {
        return fromAccount;
    }

    public HandelsbankenSEPaymentAccount getToAccount() {
        return toAccount;
    }

    public HandelsbankenAmount getAmount() {
        return amount;
    }
}
