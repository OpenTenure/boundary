package org.sola.opentenure.services.boundary.beans.report;

import java.util.Iterator;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.sola.cs.services.ejb.system.repository.entities.ReportDescription;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.security.ActiveUserBean;

/**
 * Provides methods to show report parameters and run requested report
 */
@Named
@ViewScoped
public class ReportPageBean extends AbstractBackingBean {
    
    private String reportId;
    private String reportFormat;
    private ReportParam[] paramsArray;
    private ReportDescription report;
    
    @Inject
    MessageProvider msgProvider;
    
    @Inject
    ActiveUserBean activeUser;
    
    @Inject
    ReportBean reportBean;
    
    public String getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    public ReportParam[] getParamsArray() {
        return paramsArray;
    }

    public ReportDescription getReport() {
        return report;
    }
        
    @PostConstruct
    private void init() {
        // Check user has rights to access the page
        if(!activeUser.getCanViewReports()){
            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.GENERAL_INSUFFICIENT_RIGHTS)));
            return;
        }
        
        reportId = getRequestParam("report");
        report = reportBean.getReport(reportId);
        
        if(report == null){
            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.REPORT_NOT_FOUND)));
            return;
        }
        
        // Get parameters
        List<ReportParam> params = reportBean.getReportParams(reportId);
        if (params != null) {
            // Exlude invisible params and language param
            for (Iterator<ReportParam> it = params.iterator(); it.hasNext();) {
                ReportParam next = it.next();
                if (!next.isVisible() || next.getId().equalsIgnoreCase("lang") || next.getId().equalsIgnoreCase("projectId")) {
                    it.remove();
                }
            }
            paramsArray = params.toArray(new ReportParam[params.size()]);
        }
    }
    
    public ReportParamOption[] getParamOptions(ReportParam param) {
        return param.getOptions().toArray(new ReportParamOption[param.getOptions().size()]);
    }

    public void runReport() {
        reportBean.runReport(reportId, paramsArray, reportFormat);
    }
}
