package se.tink.backend.core;

import java.util.List;

public class County {
    private String code;
    private String name;
    private List<Municipality> municipalities;
    
    public County() {
        
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<Municipality> getMunicipalities() {
        return municipalities;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMunicipalities(List<Municipality> municipalities) {
        this.municipalities = municipalities;
    }
}
