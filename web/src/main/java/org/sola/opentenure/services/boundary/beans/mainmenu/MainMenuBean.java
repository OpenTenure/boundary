package org.sola.opentenure.services.boundary.beans.mainmenu;

import java.util.List;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import org.sola.common.StringUtility;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.cs.services.ejb.cache.businesslogic.CacheCSEJBLocal;
import org.sola.cs.services.ejb.refdata.entities.ReportGroup;
import org.sola.cs.services.ejb.system.repository.entities.ReportDescription;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.opentenure.services.boundary.beans.referencedata.ReferenceData;
import org.sola.opentenure.services.boundary.beans.report.ReportBean;

/**
 * Main menu bean methods
 */
@Named
@RequestScoped
public class MainMenuBean extends AbstractBackingBean {

    private final String SHOW_REPORTS = "SHOW_REPORTS";
    private final String REPORT_MENU_ITEMS = "REPORT_MENU_ITEMS";

    @Inject
    ReportBean reportBean;

    @EJB
    CacheCSEJBLocal cacheEjb;

    @Inject
    ReferenceData refData;

    @Inject
    LanguageBean langBean;

    public MainMenuBean() {
        super();
    }

    public ReportGroup[] getReportGroups() {
        return refData.getReportGroups(true, langBean.getLocale());
    }

    public ReportDescription[] getReportsByGroup(String groupCode) {
        List<ReportDescription> allReports = getReports();
        if (allReports != null) {
            List<ReportDescription> reports = new ArrayList<>();
            for (ReportDescription report : allReports) {
                if (StringUtility.empty(report.getGroupCode()).equals(StringUtility.empty(groupCode))) {
                    reports.add(0, report);
                }
            }
            if (!reports.isEmpty()) {
                return reports.toArray(ReportDescription[]::new);
            }
        }
        return new ReportDescription[]{};
    }

    public List<ReportDescription> getReports() {
        if (cacheEjb.containsKey(REPORT_MENU_ITEMS)) {
            return (List<ReportDescription>) cacheEjb.get(REPORT_MENU_ITEMS);
        } else {
            List<ReportDescription> allReports = reportBean.getReports();
            if (allReports != null) {
                List<ReportDescription> reports = new ArrayList<>();
                for (ReportDescription report : allReports) {
                    if (report.getDisplayInMenu()) {
                        reports.add(0, report);
                    }
                }
                if (!reports.isEmpty()) {
                    cacheEjb.put(REPORT_MENU_ITEMS, reports);
                    return reports;
                }
            }
            return null;
        }
    }

    public boolean getShowReports() {
        if (cacheEjb.containsKey(SHOW_REPORTS)) {
            return (boolean) cacheEjb.get(SHOW_REPORTS);
        } else {
            List<ReportDescription> reports = getReports();
            boolean show = reports != null && !reports.isEmpty();

            cacheEjb.put(SHOW_REPORTS, show);
            return show;
        }
    }

    /**
     * Returns true if application URL contains path.
     */
    public boolean containsPath(String path) {
        if (path.equalsIgnoreCase("/index.xhtml") && getRequest().getRequestURL().toString().equalsIgnoreCase(getApplicationUrl() + "/")) {
            return true;
        }
        return getRequest().getRequestURL().toString().startsWith(getApplicationUrl() + path);
    }

    /**
     * Returns menu item class based on provided path
     */
    public String getItemClassByPath(String path) {
        if (containsPath(path)) {
            return "selectedMenuItem";
        } else {
            return "";
        }
    }

    /**
     * Returns menu hyper link class based on provided path
     */
    public String getLinkClassByPath(String path) {
        if (containsPath(path)) {
            return "padding-bottom:11px !important;";
        } else {
            return "";
        }
    }
}
