package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ViewListEntity {
    private String viewId;
    private String viewName;
    private String viewType;
    private boolean flagDefault;
    private List<ViewDetailListEntity> viewDetailList;
}
