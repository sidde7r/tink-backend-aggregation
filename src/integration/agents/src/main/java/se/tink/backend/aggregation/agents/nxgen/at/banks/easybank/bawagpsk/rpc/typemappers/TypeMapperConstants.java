package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.typemappers;

import se.tink.backend.aggregation.agents.utils.log.LogTag;

public final class TypeMapperConstants {
    private TypeMapperConstants() {
        throw new AssertionError();
    }

    public enum LogTags {
        RESPONSE_NOT_OK,
        TRANSACTION_UNKNOWN_PRODUCT_TYPE;

        public LogTag toTag() {
            return LogTag.from(name());
        }
    }
}
