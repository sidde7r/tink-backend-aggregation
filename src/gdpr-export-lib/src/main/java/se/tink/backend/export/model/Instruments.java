package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportInstrument;

public class Instruments {

    private final List<ExportInstrument> instruments;

    public Instruments(
            List<ExportInstrument> instruments) {
        this.instruments = instruments;
    }

    public List<ExportInstrument> getInstruments() {
        return instruments;
    }
}
