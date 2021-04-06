package org.sola.opentenure.services.boundary.beans.report;

/**
 * Report parameter option
 */
public class ReportParamOption {
    private String label;
    private boolean selected;
    private String value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public ReportParamOption(){
        
    }
}
