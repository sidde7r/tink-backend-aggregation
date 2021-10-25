package se.tink.backend.aggregation.storage.logs.handlers;

import lombok.experimental.UtilityClass;
import se.tink.backend.aggregation.configuration.models.configuration.S3StorageConfiguration;

@UtilityClass
public class AgentHttpLogsConstants {

    public enum AgentDebugLogBucket {
        /** Typical logs in raw format that we use to create AAP logs for Wire Mocks. */
        RAW_FORMAT_LOGS {
            @Override
            public String getBucketName(S3StorageConfiguration storageConfiguration) {
                return storageConfiguration.getAgentHttpRawLogsBucketName();
            }
        },

        /** Logs in JSON format that are meant to be queried with Athena. */
        JSON_FORMAT_LOGS {
            @Override
            public String getBucketName(S3StorageConfiguration storageConfiguration) {
                return storageConfiguration.getAgentHttpJsonLogsBucketName();
            }
        };

        public abstract String getBucketName(S3StorageConfiguration storageConfiguration);
    }

    public enum RawHttpLogsCatalog {
        /** Default catalog */
        DEFAULT,
        /** Payments' long term catalog */
        LTS_PAYMENTS
    }
}
