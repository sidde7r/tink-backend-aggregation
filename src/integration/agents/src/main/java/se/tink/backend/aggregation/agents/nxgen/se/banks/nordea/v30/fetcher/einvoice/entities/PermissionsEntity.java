package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities;

import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PermissionsEntity {
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
