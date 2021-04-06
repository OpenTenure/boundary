package org.sola.cs.services.boundary.transferobjects.claim;

import java.util.Date;
import java.util.List;
import org.sola.services.common.contracts.AbstractReadWriteTO;

public class SpatialUnitTO extends AbstractReadWriteTO {
    private String id;
    private String dimensionCode;
    private String label;
    private String surfaceRelationCode;
    private String levelId;
    private String levelName;
    private String landUseCode;
    private String geom;
    String serverUrl;
    
    public SpatialUnitTO(){
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

    public void setDimensionCode(String nr) {
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

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
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

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
