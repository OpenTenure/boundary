package org.sola.opentenure.services.boundary.beans.map;

import java.util.HashMap;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.sola.common.ConfigConstants;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.cs.services.ejb.cache.businesslogic.CacheCSEJBLocal;
import org.sola.cs.services.ejb.search.businesslogic.SearchCSEJBLocal;
import org.sola.cs.services.ejb.search.repository.entities.ConfigMapLayer;
import org.sola.cs.services.ejb.search.repository.entities.ConfigMapLayerMetadata;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.sola.opentenure.services.boundary.beans.project.ProjectBean;

/**
 * Contains methods to extract map settings to display claims and all relevant
 * layers
 */
@Named
@SessionScoped
public class MapSettingsBean extends AbstractBackingBean {

    @EJB
    SearchCSEJBLocal searchEjb;

    @Inject
    LanguageBean langBean;

    @EJB
    SystemCSEJBLocal systemEjb;

    @Inject
    ProjectBean projectBean;

    @EJB
    CacheCSEJBLocal cacheEjb;

    private final String MAP_NORTH = "MAP_NORTH";
    private final String MAP_SOUTH = "MAP_SOUTH";
    private final String MAP_WEST = "MAP_WEST";
    private final String MAP_EAST = "MAP_EAST";
    private final String MAP_EPSG = "MAP_EPSG";
    private final String MAP_PRINT_PROJ4 = "MAP_PRINT_PROJ4";
    private final String MAP_COMMUNITY_AREA = "MAP_COMMUNITY_AREA";
    private final String CS_OFFLINE_MODE = "CS_OFFLINE_MODE";
    private final String LEGEND_OPTIONS = "LEGEND_OPTIONS";

    public boolean getIsOffline() {
        if (!cacheEjb.containsKey(CS_OFFLINE_MODE)) {
            init();
        }
        return cacheEjb.get(CS_OFFLINE_MODE).toString().equals("1");
    }

    public String getCommunityArea() {
        if (!cacheEjb.containsKey(MAP_COMMUNITY_AREA)) {
            init();
        }
        return cacheEjb.get(MAP_COMMUNITY_AREA);
    }

    public String getProjectId() {
        return projectBean.getProjectId();
    }

    public String getExtTopX() {
        if (!cacheEjb.containsKey(MAP_WEST)) {
            init();
        }
        return cacheEjb.get(MAP_WEST);
    }

    public String getExtTopY() {
        if (!cacheEjb.containsKey(MAP_NORTH)) {
            init();
        }
        return cacheEjb.get(MAP_NORTH);
    }

    public String getExtBottomX() {
        if (!cacheEjb.containsKey(MAP_EAST)) {
            init();
        }
        return cacheEjb.get(MAP_EAST);
    }

    public String getExtBottomY() {
        if (!cacheEjb.containsKey(MAP_SOUTH)) {
            init();
        }
        return cacheEjb.get(MAP_SOUTH);
    }

    public String getEpsg() {
        if (!cacheEjb.containsKey(MAP_EPSG)) {
            init();
        }
        return cacheEjb.get(MAP_EPSG);
    }

    public String getMapPrintProj4() {
        if (!cacheEjb.containsKey(MAP_PRINT_PROJ4)) {
            init();
        }
        return cacheEjb.get(MAP_PRINT_PROJ4);
    }

    public List<ConfigMapLayer> getProjectLayers() {
        return searchEjb.getMapLayersByProject(projectBean.getProjectId(), langBean.getLocale());
    }

    public ConfigMapLayer[] getLayersArray() {
        List<ConfigMapLayer> layers = getProjectLayers();
        if (layers == null) {
            return null;
        }
        return layers.toArray(new ConfigMapLayer[layers.size()]);
    }

    public ConfigMapLayerMetadata[] getLayerParams(ConfigMapLayer layer) {
        if (layer == null || layer.getMetadataList() == null) {
            return null;
        }
        return layer.getMetadataList().toArray(new ConfigMapLayerMetadata[layer.getMetadataList().size()]);
    }

    /**
     * Returns WMS layer parameters sent to the server.
     */
    public String getLayerParamsString(ConfigMapLayer layer, boolean addCommaInFront) {
        if (layer == null || layer.getMetadataList() == null) {
            return "";
        }

        String result = "";

        for (ConfigMapLayerMetadata param : layer.getMetadataList()) {
            if (!param.isForClient() && layer.getTypeCode().equalsIgnoreCase("wms")
                    && !param.getName().equalsIgnoreCase(LEGEND_OPTIONS)) {
                if (!result.equals("")) {
                    result += ", ";
                }
                result += param.getName() + ": '" + param.getValue() + "'";
            }
        }

        if (!result.equals("") && addCommaInFront) {
            result = ", " + result;
        }
        return result;
    }

    /**
     * Returns WMS layer options used by map component.
     */
    public String getLayerOptionsString(ConfigMapLayer layer, boolean addCommaInFront) {
        if (layer == null || layer.getMetadataList() == null) {
            return "";
        }

        String result = "";

        for (ConfigMapLayerMetadata param : layer.getMetadataList()) {
            if (param.isForClient() && layer.getTypeCode().equalsIgnoreCase("wms")
                    && !param.getName().equalsIgnoreCase(LEGEND_OPTIONS)) {
                if (!result.equals("")) {
                    result += ", ";
                }
                result += param.getName() + ": '" + param.getValue() + "'";
            }
        }

        if (!result.equals("") && addCommaInFront) {
            result = ", " + result;
        }
        return result;
    }

    /**
     * Return WMS legend options
     */
    public String getLegendOptions(ConfigMapLayer layer) {
        if (layer == null || layer.getMetadataList() == null) {
            return "";
        }

        for (ConfigMapLayerMetadata param : layer.getMetadataList()) {
            if (param.getName().equalsIgnoreCase(LEGEND_OPTIONS)) {
                return param.getValue();
            }
        }

        return "''";
    }

    @PostConstruct
    public void init() {
        if (projectBean.getCurrentProject() != null) {
            cacheEjb.put(MAP_COMMUNITY_AREA, projectBean.getCurrentProject().getBoundary());
            cacheEjb.put(CS_OFFLINE_MODE, systemEjb.getSetting(ConfigConstants.OT_OFFLINE_MODE, projectBean.getProjectId(), "0"));
            cacheEjb.put(MAP_PRINT_PROJ4, systemEjb.getSetting(ConfigConstants.OT_TITLE_PLAN_CRS_PROJ4, projectBean.getProjectId(), ""));
        }
    }

    public MapSettingsBean() {
        super();
    }
}
