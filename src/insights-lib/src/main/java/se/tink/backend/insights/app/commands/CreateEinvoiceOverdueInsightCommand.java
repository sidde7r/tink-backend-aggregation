package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import se.tink.backend.insights.core.valueobjects.EInvoice;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CreateEinvoiceOverdueInsightCommand {

    private UserId userId;
    private List<EInvoice> overdueEinvoices;

    public CreateEinvoiceOverdueInsightCommand(UserId userId, List<EInvoice> overdueEinvoices) {
        validate(userId, overdueEinvoices);
        this.userId = userId;
        this.overdueEinvoices = overdueEinvoices;
    }

    public UserId getUserId() {
        return userId;
    }

    public List<EInvoice> getOverdueEinvoices() {
        return overdueEinvoices;
    }

    private void validate(UserId userId, List<EInvoice> overdueEinvoices) {
        Preconditions.checkArgument(!Objects.isNull(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.value()));
        Preconditions.checkArgument(overdueEinvoices.size()>0);
    }
}

