package org.sola.opentenure.services.boundary.beans.mainmenu;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;

/**
 * Main menu bean methods
 */
@Named
@RequestScoped
public class MainMenuBean extends AbstractBackingBean {
    public MainMenuBean(){
        super();
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
