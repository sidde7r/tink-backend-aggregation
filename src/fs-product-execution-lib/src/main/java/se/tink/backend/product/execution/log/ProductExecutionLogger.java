package se.tink.backend.product.execution.log;

import java.util.UUID;

public class ProductExecutionLogger extends se.tink.libraries.log.LogUtils {
    public ProductExecutionLogger(Class clazz) {
        super(clazz);
    }

    public void info(Builder builder) {
        log.info(builder.message(), builder.t);
    }

    public void debug(Builder builder) {
        log.debug(builder.message(), builder.t);
    }

    public void warn(Builder builder) {
        log.warn(builder.message(), builder.t);
    }
    public void error(Builder builder) {
        log.error(builder.message(), builder.t);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String applicationId;
        private String userId;
        private String productInstanceId;
        private String message;
        private Throwable t;

        Builder(String applicationId, String userId, String productInstanceId, String message, Throwable t) {
            this.applicationId = applicationId;
            this.userId = userId;
            this.productInstanceId = productInstanceId;
            this.message = message;
            this.t = t;
        }

        Builder() {
        }

        public Builder withApplicationId(UUID applicationId) {
            return new Builder(applicationId.toString(), userId, productInstanceId, message, t);
        }

        public Builder withUserId(UUID userId) {
            return new Builder(applicationId, userId.toString(), productInstanceId, message, t);
        }

        public Builder withMessage(String message) {
            return new Builder(applicationId, userId, productInstanceId, message, t);
        }

        public Builder withProductInstanceId(UUID productInstanceId) {
            return new Builder(applicationId, userId, productInstanceId.toString(), message, t);
        }

        public Builder withThrowable(Throwable t) {
            return new Builder(applicationId, userId, productInstanceId, message, t);
        }

        String message() {
            StringBuilder stringBuilder = new StringBuilder();
            if (userId != null) {
                stringBuilder.append(String.format("[userId: %s] ", userId));
            }
            if (applicationId != null) {
                stringBuilder.append(String.format("[applicationId: %s] ", applicationId));
            }
            if (productInstanceId != null) {
                stringBuilder.append(String.format("[productInstanceId: %s] ", productInstanceId));
            }

            if (message != null) {
                stringBuilder.append(String.format("message: %s", message));
            }

            return stringBuilder.toString();
        }
    }
}
