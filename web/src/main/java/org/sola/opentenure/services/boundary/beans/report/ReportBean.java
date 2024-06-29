package org.sola.opentenure.services.boundary.beans.report;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.sola.common.ConfigConstants;
import org.sola.common.DateUtility;
import org.sola.common.StringUtility;
import org.sola.cs.services.ejb.cache.businesslogic.CacheCSEJBLocal;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.sola.cs.services.ejb.system.repository.entities.ReportDescription;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.exceptions.OTWebException;
import org.sola.opentenure.services.boundary.beans.helpers.DateBean;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.opentenure.services.boundary.beans.project.ProjectBean;
import org.sola.services.common.logging.LogUtility;

/**
 * Provides methods to handle reports
 *
 * @author solov
 */
@Named
@ViewScoped
public class ReportBean extends AbstractBackingBean {

    private final String REPORT_ITEMS = "REPORT_ITEMS";

    @Inject
    LanguageBean langBean;

    @EJB
    CacheCSEJBLocal cacheEjb;

    @EJB
    SystemCSEJBLocal systemEjb;
    
    @Inject
    ProjectBean projectBean;
    
    @Inject
    DateBean dateBean;
    
    @Inject
    MessageProvider msgProvider;

    @PostConstruct
    private void init() {
        try {
            // Check if reports folder exists. If not, create and copy default reports.
            String reportsFolderPath = getReportsFolderPath();

            // Check if folder exists
            File reportsFolder = new File(reportsFolderPath);

            if (!reportsFolder.exists()) {
                // Create folder
                reportsFolder.mkdirs();

                // Copy default reports
                Set<String> projectReportFolders = getExtContext().getResourcePaths("/WEB-INF/classes/reports");

                for (String projectReportFolder : projectReportFolders) {
                    // Copy folder content
                    String projectReportFolderName = projectReportFolder.substring(projectReportFolder.substring(0, projectReportFolder.length() - 1).lastIndexOf("/") + 1, projectReportFolder.length() - 1);
                    File folder = new File(reportsFolderPath + "/" + projectReportFolderName);

                    // Create subfolder in the destination reports folder
                    if (!folder.exists()) {
                        folder.mkdir();
                    }

                    // Get files of the project report folder
                    Set<String> files = getExtContext().getResourcePaths(projectReportFolder);
                    for (String file : files) {
                        InputStream stream = null;
                        OutputStream resStreamOut = null;
                        String fileName = file.substring(file.lastIndexOf("/") + 1, file.length());

                        try {
                            stream = ReportBean.class.getResourceAsStream("/reports/" + projectReportFolderName + "/" + fileName);
                            int readBytes;
                            byte[] buffer = new byte[4096];
                            resStreamOut = new FileOutputStream(reportsFolderPath + "/" + projectReportFolderName + "/" + fileName);
                            while ((readBytes = stream.read(buffer)) > 0) {
                                resStreamOut.write(buffer, 0, readBytes);
                            }
                        } finally {
                            stream.close();
                            resStreamOut.close();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ReportBean.class.getName()).log(Level.SEVERE, null, ex);
            getContext().addMessage(null, new FacesMessage(processException(ex, langBean.getLocale()).getMessage()));
        }
    }

    private String getReportsFolderPath() throws Exception {
        String reportsFolderPath = systemEjb.getSetting(ConfigConstants.REPORTS_FOLDER_PATH, projectBean.getProjectId(), "");
        if (StringUtility.isEmpty(reportsFolderPath)) {
            throw new OTWebException(msgProvider.getErrorMessage(ErrorKeys.REPORTS_FOLDER_PATH_IS_NULL) + "\r\n");
        }

        // Remove ending "/" or "\"
        if ((reportsFolderPath.lastIndexOf("/") == reportsFolderPath.length() - 1)
                || (reportsFolderPath.lastIndexOf("\\") == reportsFolderPath.length() - 1)) {
            reportsFolderPath = reportsFolderPath.substring(0, reportsFolderPath.length() - 1);
        }

        // Check if the path is absolute
        Pattern pattern = Pattern.compile(reportsFolderPath, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher("^([\\/])|([a-zA-Z]:)");

        if (!matcher.find()) {
            // Path is reletive. Let's append it to the application path
            Path path = Paths.get(getExtContext().getRealPath("/"), reportsFolderPath);
            reportsFolderPath = path.toString();
        }

        return reportsFolderPath;
    }

    /**
     * Returns list of all reports
     *
     * @return
     */
    public List<ReportDescription> getReports() {
        if (cacheEjb.containsKey(REPORT_ITEMS)) {
            return (List) cacheEjb.get(REPORT_ITEMS);
        } else {
            List<ReportDescription> reports = systemEjb.getAllReports(langBean.getLocale());
            if (reports != null) {
                cacheEjb.put(REPORT_ITEMS, reports);
                return reports;
            }
            return null;
        }
    }

    /**
     * Returns report parameters by report ID
     *
     * @param reportId
     * @return
     */
    public List<ReportParam> getReportParams(String reportId) {
        try {
            List<ReportParam> params = new ArrayList<>();
            ReportDescription report = getReport(reportId);

            // Get report file
            File reportFile = getReportFile(report);
            JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(reportFile.getAbsolutePath());

            JRParameter[] jParams = jasperReport.getParameters();
            for (JRParameter jParam : jParams) {
                if (!jParam.isSystemDefined()) {
                    ReportParam param = new ReportParam();
                    param.setId(jParam.getName());
                    if (!StringUtility.isEmpty(jParam.getDescription())) {
                        param.setDescription(jParam.getDescription());
                        param.setLabel(jParam.getDescription());
                    } else {
                        param.setLabel(jParam.getName());
                    }

                    param.setReadOnly(jParam.isSystemDefined());
                    param.setVisible(jParam.isForPrompting());
                    param.setType(jParam.getValueClassName());

                    params.add(param);
                }
            }
            return params;
        } catch (Exception e) {
            LogUtility.log(e.getMessage());
            getContext().addMessage(null, new FacesMessage(processException(e, langBean.getLocale()).getMessage()));
            return null;
        }
    }

    private File getReportFile(ReportDescription report) throws Exception {
        String fileName = report.getFileName();
        if (!fileName.endsWith(".jasper")) {
            fileName += ".jasper";
        }

        String reportsFolderPath = getReportsFolderPath();
        File reportFile = new File(reportsFolderPath + "/" + report.getId() + "/" + fileName);
        if (!reportFile.exists()) {
            throw new OTWebException(msgProvider.getErrorMessage(ErrorKeys.REPORT_NOT_FOUND) + "\r\n");
        }
        return reportFile;
    }

    /**
     * Returns report by ID
     *
     * @param reportId Report ID
     * @return
     */
    public ReportDescription getReport(String reportId) {
        List<ReportDescription> reports = getReports();
        if (reports != null && !reports.isEmpty()) {
            for (ReportDescription report : reports) {
                if (report.getId().equalsIgnoreCase(reportId)) {
                    return report;
                }
            }
        }
        throw new OTWebException(msgProvider.getErrorMessage(ErrorKeys.REPORT_NOT_FOUND) + "\r\n");
    }

    /**
     * Runs report with provided parameters and output format.
     *
     * @param reportId Report ID
     * @param paramsArray Report parameters
     * @param format Output format
     */
    public void runReport(String reportId, ReportParam[] paramsArray, String format) {
        try {
            ReportDescription report = getReport(reportId);

            String contentType = "text/html";
            if (format.equalsIgnoreCase("pdf")) {
                contentType = "application/pdf";
            } else if (format.equalsIgnoreCase("docx")) {
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else if (format.equalsIgnoreCase("xlsx")) {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }

            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

            response.reset();
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "inline; filename=\"report." + format + "\"");

            // Get report file
            File reportFile = getReportFile(report);
            String reportsFolderPath = getReportsFolderPath();
            File reportFolder = new File(reportsFolderPath + "/" + report.getId());
            JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(reportFile.getAbsolutePath());
            jasperReport.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            jasperReport.setProperty("net.sf.jasperreports.default.font.name", "Sans Serif");

            // Set params
            HashMap params = new HashMap();
            String projectLocale = projectBean.getProjectLocale();
            if(!StringUtility.isEmpty(projectLocale)) {
                params.put("REPORT_LOCALE", langBean.getJavaLocale(projectLocale));
            } else {
                params.put("REPORT_LOCALE", langBean.getJavaLocale());
            }
            
            ClassLoader cl = new URLClassLoader(new URL[]{reportFolder.toURI().toURL()});
            params.put(JRParameter.REPORT_CLASS_LOADER, cl);

            if (paramsArray != null && paramsArray.length > 0) {
                for (ReportParam param : paramsArray) {
                    if (param.getValue() != null) {
                        params.put(param.getId(), param.getValue());
                    } else {
                        if (param.getValueBoolean() != null) {
                            params.put(param.getId(), param.getValueBoolean());
                        }
                        else if (StringUtility.isEmpty(param.getValueString())) {
                            params.put(param.getId(), null);
                        } else {
                            Date dt;
                            switch (param.getType()) {
                                case "java.lang.Integer":
                                    params.put(param.getId(), Integer.valueOf(param.getValueString()));
                                    break;
                                case "java.lang.Long":
                                    params.put(param.getId(), Long.valueOf(param.getValueString()));
                                    break;
                                case "java.lang.Short":
                                    params.put(param.getId(), Short.valueOf(param.getValueString()));
                                    break;
                                case "java.lang.Float":
                                    params.put(param.getId(), Float.valueOf(param.getValueString()));
                                    break;
                                case "java.lang.BigDecimal":
                                    params.put(param.getId(), BigDecimal.valueOf(Double.valueOf(param.getValueString())));
                                    break;
                                case "java.lang.Double":
                                    params.put(param.getId(), Double.valueOf(param.getValueString()));
                                    break;
                                case "java.sql.Date":
                                    dt = DateUtility.convertToDate(param.getValueString(), dateBean.getDatePattern());
                                    java.sql.Date sqldt = new java.sql.Date(dt.getTime());
                                    params.put(param.getId(), sqldt);
                                    break;
                                case "java.util.Date":
                                    dt = DateUtility.convertToDate(param.getValueString(), dateBean.getDatePattern());
                                    params.put(param.getId(), dt);
                                    break;
                                case "java.sql.Timestamp":
                                    dt = DateUtility.convertToDate(param.getValueString(), dateBean.getDatePattern());
                                    java.sql.Timestamp ts = new Timestamp(dt.getTime());
                                    params.put(param.getId(), Integer.getInteger(param.getValueString()));
                                    break;
                                default:
                                    params.put(param.getId(), param.getValueString());
                            }
                        }
                    }
                }
            }

            // Make sure lang is overriden
            if(!StringUtility.isEmpty(projectLocale)) {
                params.put("lang", projectLocale);
            } else {
                params.put("lang", langBean.getLocale());
            }
            
            // Set project id param
            params.put("projectId", projectBean.getProjectId());
            
            JasperPrint reportPrint = null;
            Connection conn = null;

            try {
                InitialContext ic = new InitialContext();
                Context dbContext = (Context) ic.lookup("java:comp/env");
                DataSource dataSource = (DataSource) dbContext.lookup("jdbc/sola");
                conn = dataSource.getConnection();
                reportPrint = JasperFillManager.fillReport(jasperReport, params, conn);
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (Exception ex) {
                    LogUtility.log(ex.getMessage());
                }
            }

            try (OutputStream output = response.getOutputStream()) {
                try {
                    final Exporter exporter;
                    boolean html = false;

                    if (format.equalsIgnoreCase("pdf")) {
                        exporter = new JRPdfExporter();
                    } else if (format.equalsIgnoreCase("docx")) {
                        exporter = new JRDocxExporter();
                    } else if (format.equalsIgnoreCase("csv")) {
                        exporter = new JRCsvExporter();
                    } else if (format.equalsIgnoreCase("xml")) {
                        exporter = new JRXmlExporter();
                    } else if (format.equalsIgnoreCase("html")) {
                        exporter = new HtmlExporter();
                        exporter.setExporterOutput(new SimpleHtmlExporterOutput(output));
                        html = true;
                    } else if (format.equalsIgnoreCase("xlsx")) {
                        exporter = new JRXlsxExporter();
                    } else {
                        throw new OTWebException("Unknown report format: " + format);
                    }

                    if (!html) {
                        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));
                    }

                    exporter.setExporterInput(new SimpleExporterInput(reportPrint));
                    exporter.exportReport();

                    output.flush();
                } catch (Exception ex) {
                    getContext().addMessage(null, new FacesMessage(ex.getMessage()));
                }
            }

            facesContext.responseComplete();

        } catch (Exception e) {
            LogUtility.log(e.getMessage());
            getContext().addMessage(null, new FacesMessage(e.getMessage()));
        }
    }
}
