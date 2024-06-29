package org.sola.opentenure.services.boundary.beans.project;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sola.opentenure.services.boundary.beans.language.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.sola.common.ConfigConstants;
import org.sola.common.StringUtility;
import org.sola.cs.services.ejb.cache.businesslogic.CacheCSEJBLocal;
import org.sola.cs.services.ejb.refdata.entities.Language;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.cs.services.ejb.search.businesslogic.SearchCSEJBLocal;
import org.sola.cs.services.ejb.search.repository.entities.ProjectNameSearchResult;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.sola.cs.services.ejb.system.repository.entities.Project;
import org.sola.opentenure.services.boundary.beans.map.MapSettingsBean;

@Named
@SessionScoped
public class ProjectBean extends AbstractBackingBean {

    private static final String PROJECT_ID = "ot_project_id";

    @Inject
    LanguageBean langBean;

    @EJB
    SearchCSEJBLocal searchEjb;

    @EJB
    SystemCSEJBLocal systemEjb;

    @Inject
    MapSettingsBean mapSettings;

    @EJB
    CacheCSEJBLocal cacheEjb;

    private Project currentProject;
    private Language projectLanguage;
    private ProjectNameSearchResult[] projectNames;
    private ProjectNameSearchResult[] allProjects;

    public void init() {
        currentProject = null;
        loadUserProjectNames();
    }

    public Project getCurrentProject() {
        if (currentProject != null) {
            return currentProject;
        }

        if (projectNames == null || projectNames.length < 1) {
            return null;
        }

        // Get project ID from cookie
        String projectId = "";

        Map<String, Object> cookies = FacesContext.getCurrentInstance().getExternalContext().getRequestCookieMap();
        if (cookies != null) {
            Cookie projectCookie = (Cookie) cookies.get(PROJECT_ID);
            if (projectCookie != null && projectCookie.getValue() != null) {
                projectId = projectCookie.getValue();
            }
        }

        // If id is found
        if (projectId != null && !projectId.equals("")) {
            for (ProjectNameSearchResult pName : projectNames) {
                if (pName.getId().equalsIgnoreCase(projectId)) {
                    currentProject = systemEjb.getProject(pName.getId(), langBean.getLocale());
                    break;
                }
            }
        }

        // If project not found, get first in the list of projects
        if (currentProject == null) {
            currentProject = systemEjb.getProject(projectNames[0].getId(), langBean.getLocale());
        }

        return currentProject;
    }

    public String getProjectId() {
        return getCurrentProject().getId();
    }

    public void setProjectId(String projectId) {
        if (projectId != null && !projectId.equals("") && projectNames != null && projectNames.length > 0) {
            // Search for existing project
            for (ProjectNameSearchResult pName : projectNames) {
                if (pName.getId().equalsIgnoreCase(projectId)) {
                    currentProject = systemEjb.getProject(pName.getId(), langBean.getLocale());
                    HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                    Cookie cookie = new Cookie(PROJECT_ID, pName.getId());
                    cookie.setMaxAge(315360000);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
    }

    public void changeProjectId(ValueChangeEvent e) {
        setProjectId((String) e.getNewValue());
        // Reset cache and layers
        cacheEjb.clearAll();
        mapSettings.init();

        try {
            // Reset cache and layers
            cacheEjb.clearAll();
            mapSettings.init();
            
            // Redirect to the same page to avoid postback
            HttpServletRequest req = (HttpServletRequest) getRequest();
            String qString = req.getParameter("params");
            String url = req.getRequestURI();
            if (!StringUtility.isEmpty(qString)) {
                url += "?" + qString;
            }
            getExtContext().redirect(url);
        } catch (IOException ex) {
        }
    }

    public ProjectNameSearchResult[] getProjectNames() {
        if (projectNames == null) {
            return new ProjectNameSearchResult[]{};
        }
        return projectNames;
    }
    
    public ProjectNameSearchResult[] getAllProjecNames(boolean addDummy) {
        if (allProjects == null) {
            List<ProjectNameSearchResult> pNames = searchEjb.searchAllProjects();
            if(pNames != null && !pNames.isEmpty()) {
                if(addDummy) {
                    ProjectNameSearchResult dummy = new ProjectNameSearchResult();
                    dummy.setId("");
                    dummy.setDisplayName(" ");
                    pNames.add(0, dummy);
                }
                allProjects = pNames.toArray(new ProjectNameSearchResult[pNames.size()]);
            }
        }
        return allProjects;
    }
 
    private void loadUserProjectNames() {
        List<ProjectNameSearchResult> pNames = searchEjb.searchCurrentUserProjects();
        if (pNames != null) {
            projectNames = pNames.toArray(new ProjectNameSearchResult[pNames.size()]);
        }
    }

    public String getProjectLocale() {
        if (projectLanguage != null) {
            return projectLanguage.getCode();
        }

        if (langBean == null || langBean.getActiveLanguages() == null || langBean.getActiveLanguages().length < 1) {
            return "";
        }

        String projectLocale = systemEjb.getSetting(ConfigConstants.PROJECT_LANGUAGE, getProjectId(), langBean.getLocale());

        for (Language lang : langBean.getActiveLanguages()) {
            if (lang.getCode().equalsIgnoreCase(projectLocale)) {
                projectLanguage = lang;
                break;
            }
        }

        // If still null, return default language
        if (projectLanguage == null) {
            return langBean.getLocale();
        }

        return projectLanguage.getCode();
    }
}
