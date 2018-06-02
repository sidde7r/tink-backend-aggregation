package se.tink.backend.main.mappers;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import se.tink.backend.core.DataExportRequest;

public class DataExportRequestMapper {

    static final TypeMap<DataExportRequest, se.tink.backend.rpc.DataExportRequest> convert = new ModelMapper()
            .createTypeMap(DataExportRequest.class, se.tink.backend.rpc.DataExportRequest.class);

    /**
     * Utility function to convert a core DataExportRequest to a thinner DataExportRequest exposed by the main API
     */
    public static se.tink.backend.rpc.DataExportRequest convert(DataExportRequest dataExportRequest) {
        return convert.map(dataExportRequest);
    }
}
