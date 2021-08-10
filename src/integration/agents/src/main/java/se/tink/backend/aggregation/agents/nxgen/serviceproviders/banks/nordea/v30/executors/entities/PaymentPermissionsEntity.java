package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.entities;

import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentPermissionsEntity {
    private boolean copy;
    private boolean delete;
    private ModifyEntity modify;

    public boolean canModifyFrom() {
        return Objects.nonNull(modify) && modify.isFrom();
    }

    public boolean canModifyTo() {
        return Objects.nonNull(modify) && modify.isTo();
    }

    public boolean canModifyAmount() {
        return Objects.nonNull(modify) && modify.isAmount();
    }

    public boolean canModifyDue() {
        return Objects.nonNull(modify) && modify.isDue();
    }

    public boolean canModifyMessage() {
        return Objects.nonNull(modify) && modify.isMessage();
    }
}
