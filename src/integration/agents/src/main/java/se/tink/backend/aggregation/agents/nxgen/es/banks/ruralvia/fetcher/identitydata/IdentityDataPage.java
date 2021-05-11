package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.identitydata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;

@Slf4j
@RequiredArgsConstructor
public class IdentityDataPage {

    private final Element dataContainer;

    public String getName() {
        return dataContainer.select("td:containsOwn(Nombre) + td").first().ownText();
    }

    public String getFirstSurname() {
        return dataContainer.select("td:containsOwn(Apellido 1) + td").first().ownText();
    }

    public String getLastSurname() {
        return dataContainer.select("td:containsOwn(Apellido 2) + td").first().ownText();
    }

    public LocalDate getBirthDate() {
        return extractValidBirthDate(dataContainer);
    }

    private LocalDate extractValidBirthDate(Element dataContainer) {
        LocalDate birthDate = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu");
            String date = dataContainer.select("td:containsOwn(Fecha) + td").first().ownText();
            birthDate = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            log.error("ERROR parsing the birth Date for Identity Data");
        }
        return birthDate;
    }
}
