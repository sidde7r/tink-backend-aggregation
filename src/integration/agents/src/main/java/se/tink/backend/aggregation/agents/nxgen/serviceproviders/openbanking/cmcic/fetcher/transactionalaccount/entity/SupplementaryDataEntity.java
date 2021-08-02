package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class SupplementaryDataEntity {
    @JsonProperty("acceptedAuthenticationApproach")
    private List<AcceptedAuthenticationApproachEnum> acceptedAuthenticationApproach;

    @JsonProperty("appliedAuthenticationApproach")
    private AppliedAuthenticationApproachEntity appliedAuthenticationApproach;

    @JsonProperty("scaHint")
    private ScaHintEnum scaHint;

    @JsonProperty("successfulReportUrl")
    private String successfulReportUrl;

    @JsonProperty("unsuccessfulReportUrl")
    private String unsuccessfulReportUrl;

    @JsonCreator
    private SupplementaryDataEntity(
            List<AcceptedAuthenticationApproachEnum> acceptedAuthenticationApproach,
            AppliedAuthenticationApproachEntity appliedAuthenticationApproach,
            ScaHintEnum scaHint,
            String successfulReportUrl,
            String unsuccessfulReportUrl) {
        this.acceptedAuthenticationApproach = acceptedAuthenticationApproach;
        this.appliedAuthenticationApproach = appliedAuthenticationApproach;
        this.scaHint = scaHint;
        this.successfulReportUrl = successfulReportUrl;
        this.unsuccessfulReportUrl = unsuccessfulReportUrl;
    }

    public static SupplementaryDataEntityBuilder builder() {
        return new SupplementaryDataEntityBuilder();
    }

    public SupplementaryDataEntity acceptedAuthenticationApproach(
            List<AcceptedAuthenticationApproachEnum> acceptedAuthenticationApproach) {
        this.acceptedAuthenticationApproach = acceptedAuthenticationApproach;
        return this;
    }

    public SupplementaryDataEntity addAcceptedAuthenticationApproachItem(
            AcceptedAuthenticationApproachEnum acceptedAuthenticationApproachItem) {
        if (this.acceptedAuthenticationApproach == null) {
            this.acceptedAuthenticationApproach = new ArrayList<>();
        }
        this.acceptedAuthenticationApproach.add(acceptedAuthenticationApproachItem);
        return this;
    }

    public List<AcceptedAuthenticationApproachEnum> getAcceptedAuthenticationApproach() {
        return acceptedAuthenticationApproach;
    }

    public void setAcceptedAuthenticationApproach(
            List<AcceptedAuthenticationApproachEnum> acceptedAuthenticationApproach) {
        this.acceptedAuthenticationApproach = acceptedAuthenticationApproach;
    }

    public SupplementaryDataEntity appliedAuthenticationApproach(
            AppliedAuthenticationApproachEntity appliedAuthenticationApproach) {
        this.appliedAuthenticationApproach = appliedAuthenticationApproach;
        return this;
    }

    public AppliedAuthenticationApproachEntity getAppliedAuthenticationApproach() {
        return appliedAuthenticationApproach;
    }

    public void setAppliedAuthenticationApproach(
            AppliedAuthenticationApproachEntity appliedAuthenticationApproach) {
        this.appliedAuthenticationApproach = appliedAuthenticationApproach;
    }

    public SupplementaryDataEntity scaHint(ScaHintEnum scaHint) {
        this.scaHint = scaHint;
        return this;
    }

    public ScaHintEnum getScaHint() {
        return scaHint;
    }

    public void setScaHint(ScaHintEnum scaHint) {
        this.scaHint = scaHint;
    }

    public SupplementaryDataEntity successfulReportUrl(String successfulReportUrl) {
        this.successfulReportUrl = successfulReportUrl;
        return this;
    }

    public String getSuccessfulReportUrl() {
        return successfulReportUrl;
    }

    public void setSuccessfulReportUrl(String successfulReportUrl) {
        this.successfulReportUrl = successfulReportUrl;
    }

    public SupplementaryDataEntity unsuccessfulReportUrl(String unsuccessfulReportUrl) {
        this.unsuccessfulReportUrl = unsuccessfulReportUrl;
        return this;
    }

    public String getUnsuccessfulReportUrl() {
        return unsuccessfulReportUrl;
    }

    public void setUnsuccessfulReportUrl(String unsuccessfulReportUrl) {
        this.unsuccessfulReportUrl = unsuccessfulReportUrl;
    }

    public enum AcceptedAuthenticationApproachEnum {
        REDIRECT("REDIRECT");

        private String value;

        AcceptedAuthenticationApproachEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static AcceptedAuthenticationApproachEnum fromValue(String text) {
            for (AcceptedAuthenticationApproachEnum b :
                    AcceptedAuthenticationApproachEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }

    public enum ScaHintEnum {
        NOSCAEXEMPTION("noScaExemption"),

        SCAEXEMPTION("scaExemption");

        private String value;

        ScaHintEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static ScaHintEnum fromValue(String text) {
            for (ScaHintEnum b : ScaHintEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }

    public static class SupplementaryDataEntityBuilder {

        private List<AcceptedAuthenticationApproachEnum> acceptedAuthenticationApproach;
        private AppliedAuthenticationApproachEntity appliedAuthenticationApproach;
        private ScaHintEnum scaHint;
        private String successfulReportUrl;
        private String unsuccessfulReportUrl;

        SupplementaryDataEntityBuilder() {}

        public SupplementaryDataEntityBuilder acceptedAuthenticationApproach(
                List<AcceptedAuthenticationApproachEnum> acceptedAuthenticationApproach) {
            this.acceptedAuthenticationApproach = acceptedAuthenticationApproach;
            return this;
        }

        public SupplementaryDataEntityBuilder appliedAuthenticationApproach(
                AppliedAuthenticationApproachEntity appliedAuthenticationApproach) {
            this.appliedAuthenticationApproach = appliedAuthenticationApproach;
            return this;
        }

        public SupplementaryDataEntityBuilder scaHint(ScaHintEnum scaHint) {
            this.scaHint = scaHint;
            return this;
        }

        public SupplementaryDataEntityBuilder successfulReportUrl(String successfulReportUrl) {
            this.successfulReportUrl = successfulReportUrl;
            return this;
        }

        public SupplementaryDataEntityBuilder unsuccessfulReportUrl(String unsuccessfulReportUrl) {
            this.unsuccessfulReportUrl = unsuccessfulReportUrl;
            return this;
        }

        public SupplementaryDataEntity build() {
            return new SupplementaryDataEntity(
                    acceptedAuthenticationApproach,
                    appliedAuthenticationApproach,
                    scaHint,
                    successfulReportUrl,
                    unsuccessfulReportUrl);
        }

        public String toString() {
            return "SupplementaryDataEntity.SupplementaryDataEntityBuilder(acceptedAuthenticationApproach="
                    + this.acceptedAuthenticationApproach
                    + ", appliedAuthenticationApproach="
                    + this.appliedAuthenticationApproach
                    + ", scaHint="
                    + this.scaHint
                    + ", successfulReportUrl="
                    + this.successfulReportUrl
                    + ", unsuccessfulReportUrl="
                    + this.unsuccessfulReportUrl
                    + ")";
        }
    }
}
