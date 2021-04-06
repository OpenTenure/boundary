package org.sola.cs.services.boundary.transferobjects.search;

import org.sola.services.common.contracts.AbstractTO;

public class SpatialUnitSearchResultTO extends AbstractTO {
    
    private String id;
    private String dimensionCode;
    private String label;
    private String surfaceRelationCode;
    private String levelId;
    private String landUseCode;
    private String geom;
    
    public SpatialUnitSearchResultTO(){
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDimensionCode() {
        return dimensionCode;
    }

    public void setDimensionCode(String dimensionCode) {
        this.dimensionCode = dimensionCode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSurfaceRelationCode() {
        return surfaceRelationCode;
    }

    public void setSurfaceRelationCode(String surfaceRelationCode) {
        this.surfaceRelationCode = surfaceRelationCode;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getLandUseCode() {
        return landUseCode;
    }

    public void setLandUseCode(String landUseCode) {
        this.landUseCode = landUseCode;
    }
    
    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

}
