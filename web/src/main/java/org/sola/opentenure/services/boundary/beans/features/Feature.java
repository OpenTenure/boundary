package org.sola.opentenure.services.boundary.beans.features;

/**
 * Simple feature class
 */
public class Feature {
    private String id;
    private String geometry;

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public Feature(){
    }
}
