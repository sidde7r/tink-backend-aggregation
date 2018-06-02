package se.tink.backend.core.application;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;

public class FieldData {

    private String title;
    private List<List<String>> values = Lists.newArrayList(); // List of rows. Each row is a list of columns (1 or 2).

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<List<String>> getValues() {
        return values;
    }

    private void addRow(List<String> columns) {
        // One or two columns required.
        Preconditions.checkArgument(columns != null && !columns.isEmpty() && columns.size() <= 2);
        
        getValues().add(columns);
    }
    
    public void addRow(String column1, String column2) {
        Preconditions.checkArgument(column1 != null);
        Preconditions.checkArgument(column2 != null);
        
        addRow(Lists.newArrayList(column1, column2));
    }
    
    public void addRow(String column) {
        Preconditions.checkArgument(column != null);
        
        addRow(Lists.newArrayList(column));
    }
}
