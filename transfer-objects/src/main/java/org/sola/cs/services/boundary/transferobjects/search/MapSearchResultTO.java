package org.sola.cs.services.boundary.transferobjects.search;

import org.sola.services.common.contracts.AbstractTO;

public class MapSearchResultTO extends AbstractTO {
    
    private String id;
    private String nr;
    private String ownerNames;
    private String geom;
    
    public MapSearchResultTO(){
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNr() {
        return nr;
    }

    public void setNr(String nr) {
        this.nr = nr;
    }

    public String getOwnerNames() {
        return ownerNames;
    }

    public void setOwnerNames(String ownerNames) {
        this.ownerNames = ownerNames;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }
}