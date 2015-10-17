package org.sola.opentenure.services.boundary.beans.mainmenu;

import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.report.ReportServerBean;
import org.sola.opentenure.services.boundary.beans.report.ResourceDescription;
import org.sola.cs.services.ejb.cache.businesslogic.CacheCSEJBLocal;

/**
 * Main menu bean methods
 */
@Named
@RequestScoped
public class MainMenuBean extends AbstractBackingBean {
    private final String REPORT_URLS = "REPORT_URLS";
    
    @Inject
    ReportServerBean server;
    
    @EJB
    CacheCSEJBLocal cacheEjb;
    
    public MainMenuBean(){
        super();
    }
    
    public ResourceDescription[] getReports(){
        if(cacheEjb.containsKey(REPORT_URLS)){
            return (ResourceDescription[]) cacheEjb.get(REPORT_URLS);
        } else {
            List<ResourceDescription> reports = server.getFolderReports();
            if(reports!=null){
                cacheEjb.put(REPORT_URLS, reports.toArray(new ResourceDescription[reports.size()]));
                return reports.toArray(new ResourceDescription[reports.size()]);
            }
            return null;
        }
    }
    
    /** Returns true if application URL contains path. */
    public boolean containsPath(String path){
        if(path.equalsIgnoreCase("/index.xhtml") && getRequest().getRequestURL().toString().equalsIgnoreCase(getApplicationUrl() + "/")){
            return true;
        }
        return getRequest().getRequestURL().toString().startsWith(getApplicationUrl() + path);
    }
    
    /** Returns menu item class based on provided path */
    public String getItemClassByPath(String path){
        if(containsPath(path)){
            return "selectedMenuItem";
        } else {
            return "";
        }
    }
    
    /** Returns menu hyper link class based on provided path */
    public String getLinkClassByPath(String path){
        if(containsPath(path)){
            return "padding-bottom:11px !important;";
        } else {
            return "";
        }
    }
}
