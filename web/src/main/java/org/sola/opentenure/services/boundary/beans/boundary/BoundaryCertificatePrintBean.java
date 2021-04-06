package org.sola.opentenure.services.boundary.beans.boundary;

import java.net.URLEncoder;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.sola.common.StringUtility;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import javax.inject.Inject;
import org.sola.common.ConfigConstants;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.sola.opentenure.services.boundary.beans.report.ReportParam;
import org.sola.opentenure.services.boundary.beans.report.ReportServerBean;
import org.sola.opentenure.services.boundary.beans.report.ResourceDescription;
import org.sola.services.common.logging.LogUtility;

/**
 * Returns map image with boundary
 */
@Named
@ViewScoped
public class BoundaryCertificatePrintBean extends AbstractBackingBean {

    @EJB
    SystemCSEJBLocal systemEjb;
    
    @Inject
    ReportServerBean server;

    public void printBoundaryCertificate() {
         try {
            String id = getRequestParam("id");
            if(StringUtility.isEmpty(id)){
                return;
            }
            
            ResourceDescription report = server.getReport(systemEjb.getSetting(ConfigConstants.BOUNDARY_CERTIFICATE_URL, ""));
            List<ReportParam> params = server.getReportParameters(report.getUri());
            String appUrl = systemEjb.getSetting(ConfigConstants.CS_SERVER_URL, "");
            
            if (params != null) {
                for (ReportParam param : params) {
                    if (param.getId().equalsIgnoreCase("id")) {
                        param.setValueString(id);
                    }
                    if (param.getId().equalsIgnoreCase("imageUrl")) {
                        if(StringUtility.isEmpty(appUrl)){
                            appUrl = getApplicationUrl();
                            appUrl = appUrl.replace("https:", "http:").replace(":8181", ":8080").replace(":443", "");
                        }
                        param.setValueString(URLEncoder.encode(appUrl + "/boundary/GetBoundaryMapImage.xhtml", "UTF-8"));
                    }
                }
                server.runReport(report.getUri(), params.toArray(new ReportParam[params.size()]), "pdf");
            }
        } catch (Exception e) {
            getContext().addMessage(null, new FacesMessage("Failed to run boundary certificate printing"));
            LogUtility.log("Failed to run boundary certificate printing", e);
        }
    }
}
