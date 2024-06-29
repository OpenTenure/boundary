package org.sola.opentenure.services.boundary.beans.claim;

import java.net.URLEncoder;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.sola.common.StringUtility;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import jakarta.inject.Inject;
import org.sola.common.ConfigConstants;
import org.sola.cs.services.ejb.search.businesslogic.SearchCSEJBLocal;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSearchResult;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.sola.cs.services.ejb.system.repository.entities.ReportDescription;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.project.ProjectBean;
import org.sola.opentenure.services.boundary.beans.report.ReportBean;
import org.sola.opentenure.services.boundary.beans.report.ReportParam;
import org.sola.services.common.logging.LogUtility;

/**
 * Returns map image with claims
 */
@Named
@ViewScoped
public class ClaimCertificatePrintBean extends AbstractBackingBean {

    @EJB
    SystemCSEJBLocal systemEjb;

    @EJB
    SearchCSEJBLocal searchEjb;
    
    @Inject
    MessageProvider msgProvider;
    
    @Inject
    ReportBean reportBean;
    
    @Inject
    GetMapImage mapImageBean;
    
    @Inject
    ProjectBean projectBean;

    public void printCertificate() {
        try {
            String id = getRequestParam("id");
            if (StringUtility.isEmpty(id)) {
                return;
            }
            
            ClaimSearchResult claim = searchEjb.searchClaimById(id, null);

            if(claim == null || !claim.getProjectId().equalsIgnoreCase(projectBean.getProjectId())) {
                return;
            }
            
            String reportId = systemEjb.getSetting(ConfigConstants.CLAIM_CERTIFICATE_ID, projectBean.getProjectId(), "");
            List<ReportParam> params = reportBean.getReportParams(reportId);
            String communityName = systemEjb.getSetting(ConfigConstants.COMMUNITY_NAME, projectBean.getProjectId(), "Test community");

            if (params != null) {
                for (ReportParam param : params) {
                    if (param.getId().equalsIgnoreCase("id")) {
                        param.setValueString(id);
                    }
                    if (param.getId().equalsIgnoreCase("communityName")) {
                        param.setValueString(communityName);
                    }
                    if (param.getId().equalsIgnoreCase("parcelMap")) {
                        param.setValue(mapImageBean.getMapImage(id));
                    }
                }
                reportBean.runReport(reportId, params.toArray(new ReportParam[params.size()]), "pdf");
            } else {
                getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.REPORT_NOT_FOUND)));
            }
        } catch (Exception e) {
            getContext().addMessage(null, new FacesMessage("Failed to run claim certificate printing"));
            LogUtility.log("Failed to run claim certificate printing", e);
        }
    }
}
