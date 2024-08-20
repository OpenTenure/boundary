package org.sola.cs.services.boundary.transferobjects.search;

public class AdministrativeBoundaryWithGeomSearchResultTO extends AdministrativeBoundarySearchResultTO {
    
    private String geom;
    private String parentName;
    
    public AdministrativeBoundaryWithGeomSearchResultTO(){
        super();
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
}