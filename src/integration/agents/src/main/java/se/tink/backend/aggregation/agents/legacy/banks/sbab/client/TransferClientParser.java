package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.TransferEntity;

public class TransferClientParser {

    static List<TransferEntity> parseUpcomingTransfers(Element table) {
        List<TransferEntity> upcomingTransfers = Lists.newArrayList();
        Elements tableRows = table.select("> tbody > tr");

        // Every transfer has one unnecessary row in the bottom, so row += 2 is necessary for every iteration.

        for (int row = 0; row < tableRows.size(); row += 2) {
            TransferEntity upcomingTransfer = new TransferEntity();

            Element mainRow = tableRows.get(row);
            Elements mainRowData = mainRow.select("> td");

            upcomingTransfer.setDate(mainRowData.get(1).text());
            upcomingTransfer.setNegativeAmount(mainRowData.get(2).text());

            Element infoRow = tableRows.get(++row);
            Elements infoHeaders = infoRow.select("span[class=info-text]");

            Element message = infoHeaders.select(":contains(Meddelande)").first();
            upcomingTransfer.setDestinationMessage(message.nextElementSibling().text());

            Element sourceMessage = infoHeaders.select(":contains(Egen notering)").first();
            upcomingTransfer.setSourceMessage(sourceMessage.nextElementSibling().text());

            Element fromAccount = infoHeaders.select(":contains(Från konto)").first();
            upcomingTransfer.setFromAccountNumber(fromAccount.nextElementSibling().text());

            Element destinationAccount = infoHeaders.select(":contains(Mottagare)").first();
            upcomingTransfer.setDestinationAccountNumber(destinationAccount.nextElementSibling().text());

            upcomingTransfers.add(upcomingTransfer);
        }

        return upcomingTransfers;
    }

    static List<TransferEntity> parseTransfersToAccept(Element table) {
        List<TransferEntity> transfersToAccept = Lists.newArrayList();
        Elements tableHeaders = table.select("> thead > tr > th");
        Elements tableRows = table.select("> tbody > tr");

        // Every transfer has two unnecessary rows in the bottom, so row += 3 is necessary for every iteration.

        for (int row = 0; row < tableRows.size(); row += 3) {
            TransferEntity transferToAccept = new TransferEntity();

            Element mainRow = tableRows.get(row);
            Elements mainRowData = mainRow.select("> td");
            Map<String, String> mainRowValues = Maps.newHashMap();

            for (int i = 0; i < mainRowData.size(); i++) {
                String header = tableHeaders.get(i).text();
                String data = mainRowData.get(i).text();
                mainRowValues.put(header, data);
            }

            transferToAccept.setId(mainRow.select("input[name=checkboxar]").first().attr("id"));
            transferToAccept.setNegativeAmount(mainRowValues.get("Belopp"));
            transferToAccept.setDate(mainRowValues.get("Datum"));

            Element infoRow = tableRows.get(++row);
            Elements infoHeaders = infoRow.select("span[class=info-text]");

            Element message = infoHeaders.select(":contains(Meddelande)").first();
            transferToAccept.setDestinationMessage(message.nextElementSibling().text());

            Element sourceMessage = infoHeaders.select(":contains(Egen notering)").first();
            transferToAccept.setSourceMessage(sourceMessage.nextElementSibling().text());

            Element fromAccount = infoHeaders.select(":contains(Från konto)").first();
            transferToAccept.setFromAccountNumber(fromAccount.nextElementSibling().text());

            Element destinationAccount = infoHeaders.select(":contains(Mottagare)").first();
            transferToAccept.setDestinationAccountNumber(destinationAccount.nextElementSibling().text());

            transfersToAccept.add(transferToAccept);
        }

        return transfersToAccept;
    }

}
