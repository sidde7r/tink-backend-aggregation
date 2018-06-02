package se.tink.backend.insights.app.generators;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.commands.CreateEinvoiceOverdueInsightCommand;
import se.tink.backend.insights.core.valueobjects.EInvoice;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.transfer.TransferQueryService;
import se.tink.backend.insights.utils.LogUtils;
import se.tink.libraries.date.DateUtils;

public class EinvoiceOverdueGenerator implements InsightGenerator {
    private static final LogUtils log = new LogUtils(EinvoiceOverdueGenerator.class);

    private CommandGateway gateway;
    private TransferQueryService transferQueryService;

    @Inject
    public EinvoiceOverdueGenerator(CommandGateway gateway,
            TransferQueryService transferQueryService) {
        this.gateway = gateway;
        this.transferQueryService = transferQueryService;
    }

    @Override
    public void generateIfShould(UserId userId) {

        List<EInvoice> eInvoices = transferQueryService.getEInvoices(userId);

        if (eInvoices.size() == 0) {
            log.info(userId, "No insight generated. Reason: No e-invoices found");
            return;
        }

        Date today = DateUtils.getToday();
        List<EInvoice> overdueEinvoices = eInvoices.stream().filter(e -> DateUtils.after(today, e.getDueDate()))
                .collect(Collectors.toList());

        if (overdueEinvoices.size() == 0) {
            log.info(userId, "No insight generated. Reason: No overdue e-invoices found");
            return;
        }

        CreateEinvoiceOverdueInsightCommand command = new CreateEinvoiceOverdueInsightCommand(userId, overdueEinvoices);
        gateway.on(command);
    }
}
