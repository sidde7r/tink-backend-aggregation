package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient.Creatable;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.HandelsbankenSEBankTransferExecutor.Transferable;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public abstract class TransferableResponse extends BaseResponse implements Transferable {

    @Override
    public Creatable toCreatable(ExecutorExceptionResolver exceptionResolver) {
        return () ->
                searchLink(HandelsbankenConstants.URLS.Links.CREATE)
                        .orElseThrow(
                                () ->
                                        exceptionResolver.asException(
                                                HandelsbankenSEConstants.Executor.ExceptionMessages
                                                        .INVALID_DESTINATION_ACCOUNT));
    }
}
