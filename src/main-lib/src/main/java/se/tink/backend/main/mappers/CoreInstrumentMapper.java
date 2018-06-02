package se.tink.backend.main.mappers;

import org.assertj.core.util.VisibleForTesting;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.backend.core.Instrument;

public class CoreInstrumentMapper {

    // ModelMapper for converting a core Instrument object to a Main RPC object.
    @VisibleForTesting
    static final TypeMap<Instrument, se.tink.backend.rpc.Instrument> fromCoreToMainMap = new ModelMapper()
            .createTypeMap(Instrument.class, se.tink.backend.rpc.Instrument.class);

    /**
     * Utility function to convert a core Instrument to the API Instrument for the Main service.
     */
    public static se.tink.backend.rpc.Instrument fromCoreToMain(Instrument instrument) {
        return fromCoreToMainMap.map(instrument);
    }
}
