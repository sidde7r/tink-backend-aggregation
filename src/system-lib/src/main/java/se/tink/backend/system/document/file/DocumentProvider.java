package se.tink.backend.system.document.file;

import java.util.List;
import java.util.Map;
import se.tink.backend.system.document.core.SwitchMortgageProvider;

public interface DocumentProvider {

    void generateInMemoryPdfDocumentsMap(SwitchMortgageProvider switchMortgageProvider);
    Map<String, byte[]> getFiles(String userId, String namePrefix, List<String> loanNumbers);
}
