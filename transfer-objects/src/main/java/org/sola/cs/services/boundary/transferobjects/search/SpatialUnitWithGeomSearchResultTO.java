package org.sola.cs.services.boundary.transferobjects.search;

public class SpatialUnitWithGeomSearchResultTO extends SpatialUnitSearchResultTO {
    
    private String geom;
   
    public SpatialUnitWithGeomSearchResultTO(){
        super();
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }
}