package org.sola.opentenure.services.boundary.beans.report;

import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.common.StringUtility;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.exceptions.OTWebException;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.security.ActiveUserBean;

/**
 * Provides methods to show report parameters and run requested report
 */
@Named
@ViewScoped
public class ReportPageBean extends AbstractBackingBean {
    
    private String reportUri;
    private String reportFormat;
    private ReportParam[] paramsArray;
    private ResourceDescription report;
    
    @Inject
    MessageProvider msgProvider;
    
    @Inject 
    ReportServerBean server;

    @Inject
    ActiveUserBean activeUser;
    
    public String getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }

    public ReportParam[] getParamsArray() {
        return paramsArray;
    }

    public ResourceDescription getReport() {
        return report;
    }
    
    public ReportPageBean(){
        
    }
        
    @PostConstruct
    private void init() {
        // Check user has rights to access the page
        if(!activeUser.getCanViewReports()){
            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.GENERAL_INSUFFICIENT_RIGHTS)));
            return;
        }
        
        reportUri = getRequestParam("report");
        report = server.getReport(reportUri);
        
        if(report == null){
            getContext().addMessage(null, new FacesMessage(msgProvider.getErrorMessage(ErrorKeys.REPORT_NOT_FOUND)));
            return;
        }
        
        // Get parameters
        List<ReportParam> params = server.getReportParameters(report.getUri());
        if (params != null) {
            // Exlude invisible params and language param
            for (Iterator<ReportParam> it = params.iterator(); it.hasNext();) {
                ReportParam next = it.next();
                if (!next.isVisible() || next.getId().equalsIgnoreCase("lang")) {
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
        server.runReport(report.getUri(), paramsArray, reportFormat);
    }
}
