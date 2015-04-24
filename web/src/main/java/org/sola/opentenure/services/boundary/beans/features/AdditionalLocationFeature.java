package org.sola.opentenure.services.boundary.beans.features;

/**
 * Claim additional location feature
 */
public class AdditionalLocationFeature extends Feature {
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public AdditionalLocationFeature(){
        super();
    }
}
