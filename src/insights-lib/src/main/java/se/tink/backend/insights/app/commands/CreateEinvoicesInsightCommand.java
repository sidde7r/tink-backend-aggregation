package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import se.tink.backend.insights.core.valueobjects.EInvoice;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CreateEinvoicesInsightCommand {
    private UserId userId;
    private List<EInvoice> eInvoices;

    public CreateEinvoicesInsightCommand(UserId userId, List<EInvoice> eInvoices) {
        validate(userId, eInvoices);
        this.userId = userId;
        this.eInvoices = eInvoices;
    }

    public UserId getUserId() {
        return userId;
    }

    public List<EInvoice> getEInvoices() {
        return eInvoices;
    }

    private void validate(UserId userId, List<EInvoice> eInvoices) {
        Preconditions.checkArgument(!Objects.isNull(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.value()));
        Preconditions.checkArgument(eInvoices.size() > 0);
    }
}
