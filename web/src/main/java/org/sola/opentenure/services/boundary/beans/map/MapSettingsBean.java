package org.sola.opentenure.services.boundary.beans.map;

import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.common.ConfigConstants;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.services.ejb.search.businesslogic.SearchEJBLocal;
import org.sola.services.ejb.search.repository.entities.ConfigMapLayer;
import org.sola.services.ejb.search.repository.entities.ConfigMapLayerMetadata;
import org.sola.services.ejb.search.repository.entities.Crs;
import org.sola.services.ejb.system.businesslogic.SystemEJBLocal;

/**
 * Contains methods to extract map settings to display claims and all relevant layers
 */
@Named
@SessionScoped
public class MapSettingsBean extends AbstractBackingBean {
    @EJB
    SearchEJBLocal searchEjb;
    
    @Inject
    LanguageBean langBean;
    
    @EJB
    SystemEJBLocal systemEjb;
    
    private String extTopX = "0";
    private String extTopY = "0";
    private String extBottomX = "0";
    private String extBottomY = "0";
    private String epsg = "0";
    private List<ConfigMapLayer> layers;
    private String communityArea = "";

    public String getCommunityArea() {
        return communityArea;
    }

    public void setCommunityArea(String communityArea) {
        this.communityArea = communityArea;
    }

    public String getExtTopX() {
        return extTopX;
    }

    public void setExtTopX(String extTopX) {
        this.extTopX = extTopX;
    }

    public String getExtTopY() {
        return extTopY;
    }

    public void setExtTopY(String extTopY) {
        this.extTopY = extTopY;
    }

    public String getExtBottomX() {
        return extBottomX;
    }

    public void setExtBottomX(String extBottomX) {
        this.extBottomX = extBottomX;
    }

    public String getExtBottomY() {
        return extBottomY;
    }

    public void setExtBottomY(String extBottomY) {
        this.extBottomY = extBottomY;
    }

    public String getEpsg() {
        return epsg;
    }

    public void setEpsg(String epsg) {
        this.epsg = epsg;
    }

    public List<ConfigMapLayer> getLayers() {
        return layers;
    }
    
    public ConfigMapLayer[] getLayersArray() {
        if(layers== null){
            return null;
        }
        return layers.toArray(new ConfigMapLayer[layers.size()]);
    }
    
    public ConfigMapLayerMetadata[] getLayerParams(ConfigMapLayer layer) {
        if(layer== null || layer.getMetadataList() == null){
            return null;
        }
        return layer.getMetadataList().toArray(new ConfigMapLayerMetadata[layer.getMetadataList().size()]);
    }
    
    public String getLayerParamsString(ConfigMapLayer layer, boolean addCommaInFront){
        if(layer== null || layer.getMetadataList() == null){
            return "";
        }
        
        String result = "";
        
        for(ConfigMapLayerMetadata param : layer.getMetadataList()){
            if(!result.equals("")){
                result += ", ";
            }
            // Skip legend settings for WMS layer
            if(layer.getTypeCode().equalsIgnoreCase("wms") && !param.getName().equalsIgnoreCase("LEGEND_OPTIONS")){
                result += param.getName() + ": '" + param.getValue() + "'";
            }
        }
        
        if(!result.equals("") && addCommaInFront){
            result = ", " + result;
        }
        return result;
    }
    
    /** Return WMS legend options*/
    public String getLegendOptions(ConfigMapLayer layer){
        if(layer== null || layer.getMetadataList() == null){
            return "";
        }
               
        for(ConfigMapLayerMetadata param : layer.getMetadataList()){
            if(param.getName().equalsIgnoreCase("LEGEND_OPTIONS")){
                return param.getValue();
            }
        }
        
        return "''";
    }
    
    @PostConstruct
    public void init(){
        HashMap<String, String> mapSettings = searchEjb.getMapSettingList();
        List<Crs> crs = searchEjb.getCrsList();
        layers = searchEjb.getConfigMapLayerList(langBean.getLocale());
        communityArea = systemEjb.getSetting(ConfigConstants.OT_COMMUNITY_AREA, "");
        
        if(mapSettings!=null){
            extTopX = mapSettings.get("map-west");
            extTopY = mapSettings.get("map-north");
            extBottomX = mapSettings.get("map-east");
            extBottomY = mapSettings.get("map-south");
        }
        
        if(crs!=null){
            epsg = "EPSG:" + crs.get(0).getSrid();
        }
    }
    
    public MapSettingsBean(){
        super();
    }
}
