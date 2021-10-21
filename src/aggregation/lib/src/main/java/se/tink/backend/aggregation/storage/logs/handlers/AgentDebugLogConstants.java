package se.tink.backend.aggregation.storage.logs.handlers;

import lombok.experimental.UtilityClass;
import se.tink.backend.aggregation.configuration.models.configuration.S3StorageConfiguration;

@UtilityClass
public class AgentDebugLogConstants {

    public enum AgentDebugLogBucket {
        /** Bucket for typical logs in AAP format that we use for Wire Mocks. */
        AAP_FORMAT_LOGS {
            @Override
            public String getBucketName(S3StorageConfiguration storageConfiguration) {
                return storageConfiguration.getAgentDebugBucketName();
            }
        },

        /** Bucket for http logs in JSON format that are meant to be queried with Athena. */
        JSON_FORMAT_LOGS {
            @Override
            public String getBucketName(S3StorageConfiguration storageConfiguration) {
                return storageConfiguration.getHttpJsonDebugBucketName();
            }
        };

        public abstract String getBucketName(S3StorageConfiguration storageConfiguration);
    }

    public enum AapLogsCatalog {
        /** Default catalog */
        DEFAULT,
        /** Payments' long term catalog */
        LTS_PAYMENTS
    }
}
