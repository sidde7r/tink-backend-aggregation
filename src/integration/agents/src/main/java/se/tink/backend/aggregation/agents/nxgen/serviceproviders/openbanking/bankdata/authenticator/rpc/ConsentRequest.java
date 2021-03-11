package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Calendar;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class ConsentRequest {
    @JsonProperty protected boolean recurringIndicator;
    @JsonProperty private AccessEntity access;

    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date validUntil;

    @JsonProperty private int frequencyPerDay;
    @JsonProperty private boolean combinedServiceIndicator;

    public ConsentRequest() {
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, 11);
        this.recurringIndicator = true;
        this.validUntil = now.getTime();
        this.frequencyPerDay = 4;
        this.combinedServiceIndicator = false;
        this.access =
                new AccessEntity(BankdataConstants.ConsentRequest.ALL_ACCOUNTS_WITH_OWNER_NAME);
    }

    private ConsentRequest(
            final boolean recurringIndicator,
            final Date validUntil,
            final int frequencyPerDay,
            final boolean combinedServiceIndicator,
            AccessEntity access) {
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.frequencyPerDay = frequencyPerDay;
        this.combinedServiceIndicator = combinedServiceIndicator;
        this.access = access;
    }

    public static ConsentBaseRequestBuilder builder() {
        return new ConsentBaseRequestBuilder();
    }

    public String toData() {
        return SerializationUtils.serializeToString(this);
    }

    public AccessEntity getAccess() {
        return access;
    }

    public static class ConsentBaseRequestBuilder {

        private boolean recurringIndicator;
        private Date validUntil;
        private int frequencyPerDay;
        private boolean combinedServiceIndicator;
        private AccessEntity access;

        public ConsentBaseRequestBuilder() {}

        public ConsentBaseRequestBuilder recurringIndicator(final boolean recurringIndicator) {
            this.recurringIndicator = recurringIndicator;
            return this;
        }

        public ConsentBaseRequestBuilder validUntil(final Date validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        public ConsentBaseRequestBuilder frequencyPerDay(final int frequencyPerDay) {
            this.frequencyPerDay = frequencyPerDay;
            return this;
        }

        public ConsentBaseRequestBuilder combinedServiceIndicator(
                final boolean combinedServiceIndicator) {
            this.combinedServiceIndicator = combinedServiceIndicator;
            return this;
        }

        public ConsentBaseRequestBuilder accessEntity(final AccessEntity access) {
            this.access = access;
            return this;
        }

        public ConsentRequest build() {
            return new ConsentRequest(
                    recurringIndicator,
                    validUntil,
                    frequencyPerDay,
                    combinedServiceIndicator,
                    access);
        }
    }
}
