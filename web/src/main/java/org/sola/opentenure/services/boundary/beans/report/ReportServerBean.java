package org.sola.opentenure.services.boundary.beans.report;

import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.xml.parsers.*;
import org.sola.common.ConfigConstants;
import org.sola.common.StringUtility;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.services.common.logging.LogUtility;
import org.sola.cs.services.ejb.cache.businesslogic.CacheCSEJBLocal;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Named
@ViewScoped
public class ReportServerBean extends AbstractBackingBean {

    private Client client = null;
    private String baseServerUrl;
    private String user;
    private String password;
    private String reportsFolder;
    private ArrayList<Object> cookies;

    private static final String APP_XML = "application/xml";
    private static final String UTF8 = "UTF-8";

    @EJB
    CacheCSEJBLocal cacheEjb;
    @EJB
    SystemCSEJBLocal systemEjb;
    @Inject
    MessageProvider msgProvider;
    @Inject
    LanguageBean langBean;

    /**
     * Initializes various variables related to reporting server configuration
     */
    @PostConstruct
    public void init() {
        // Server URL
        if (cacheEjb.containsKey(ConfigConstants.REPORT_SERVER_URL)) {
            baseServerUrl = cacheEjb.get(ConfigConstants.REPORT_SERVER_URL);
        } else {
            baseServerUrl = systemEjb.getSetting(ConfigConstants.REPORT_SERVER_URL, "");
            cacheEjb.put(ConfigConstants.REPORT_SERVER_URL, baseServerUrl);
        }

        // Username
        if (cacheEjb.containsKey(ConfigConstants.REPORT_SERVER_USER)) {
            user = cacheEjb.get(ConfigConstants.REPORT_SERVER_USER);
        } else {
            user = systemEjb.getSetting(ConfigConstants.REPORT_SERVER_USER, "");
            cacheEjb.put(ConfigConstants.REPORT_SERVER_USER, user);
        }

        // User password
        if (cacheEjb.containsKey(ConfigConstants.REPORT_SERVER_PASS)) {
            password = cacheEjb.get(ConfigConstants.REPORT_SERVER_PASS);
        } else {
            password = systemEjb.getSetting(ConfigConstants.REPORT_SERVER_PASS, "");
            cacheEjb.put(ConfigConstants.REPORT_SERVER_PASS, password);
        }

        // Reports folder
        if (cacheEjb.containsKey(ConfigConstants.REPORTS_FOLDER_URL)) {
            reportsFolder = cacheEjb.get(ConfigConstants.REPORTS_FOLDER_URL);
        } else {
            reportsFolder = systemEjb.getSetting(ConfigConstants.REPORTS_FOLDER_URL, "");
            cacheEjb.put(ConfigConstants.REPORTS_FOLDER_URL, reportsFolder);
        }
    }

    /**
     * Initialize Jersey client and authenticate user.
     *
     * @return
     */
    public Client getClient() {
        if (client == null) {
            client = ClientBuilder.newClient();

            client.register(new ClientResponseFilter() {
                @Override
                public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
                    if (responseContext.getCookies() != null) {
                        if (cookies == null) {
                            cookies = new ArrayList<>();
                        }
                        for (Map.Entry<String, NewCookie> cookie : responseContext.getCookies().entrySet()) {
                            if (cookie.getValue().getName().equalsIgnoreCase("JSESSIONID")) {
                                cookies.clear();
                                cookies.add(cookie.getValue());
                                break;
                            }
                        }
                    }
                }
            });

            client.register(new ClientRequestFilter() {
                @Override
                public void filter(ClientRequestContext requestContext) throws IOException {
                    if (cookies != null) {
                        requestContext.getHeaders().put("Cookie", cookies);
                    }
                }
            });

            // Authenticate
            WebTarget target = client.target(baseServerUrl + "/rest_v2/login?j_username=" + user + "&j_password=" + password);
            Response response = target.request("application/json").get();

            // AM 4 Aug 2018
            // JapserServer throws a 404 error for version 7 and 7.1, but still succeeds in authenticating. 
            if (response.getStatus() != 200 && response.getStatus() != 404) {
                throw new RuntimeException(String.format(
                        msgProvider.getErrorMessage(ErrorKeys.REPORTS_FAILED_AUTHENTICATE),
                        Integer.toString(response.getStatus())));
            }
        }
        return client;
    }

    /**
     * Returns list of reports based on OpenTenure settings folder path
     *
     * @return
     */
    public List<ResourceDescription> getFolderReports() {
        // Get reports folder path from the settings
        return getFolderReports(reportsFolder);
    }

    /**
     * Returns list of reports based on provided folder path
     *
     * @param folderPath Folder path on the reports server
     * @return
     */
    public List<ResourceDescription> getFolderReports(String folderPath) {
        try {
            if (folderPath != null && folderPath.length() > 0 && !folderPath.startsWith("/")) {
                folderPath = "/" + folderPath;
            }

            WebTarget target = getClient().target(baseServerUrl + "/rest_v2/resources?folderUri=" + folderPath);
            Response response = target.request(APP_XML).get();
            LogUtility.log("JapserReport folder URI: " + baseServerUrl + "/rest_v2/resources?folderUri=" + folderPath);
            LogUtility.log("JasperReport folder URI response:" + response.getStatus());

            if (response.getStatus() != 200) {
                getContext().addMessage(null, new FacesMessage(
                        String.format(msgProvider.getErrorMessage(ErrorKeys.REPORTS_FAILED_TO_GET_REPORTS),
                                Integer.toString(response.getStatus()))));
                return null;
            }

            String xmlString = response.readEntity(String.class);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlString.getBytes(UTF8));
            Document doc = builder.parse(input);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName(ResourceTagNameConst.RESOURCE_LOOKUP);
            List<ResourceDescription> resources = new ArrayList<>();

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                Element eElement = (Element) nNode;
                if (eElement.getElementsByTagName(ResourceTagNameConst.RESOURCE_TYPE).item(0)
                        .getTextContent().equalsIgnoreCase(ResourceTypeConst.REPORT_UNIT)) {
                    ResourceDescription resource = new ResourceDescription();
                    resource.setDescription(eElement.getElementsByTagName(ResourceTagNameConst.DESCRIPTION)
                            .item(0).getTextContent());
                    resource.setLabel(eElement.getElementsByTagName(ResourceTagNameConst.LABEL)
                            .item(0).getTextContent());
                    resource.setType(eElement.getElementsByTagName(ResourceTagNameConst.RESOURCE_TYPE)
                            .item(0).getTextContent());
                    resource.setUri(eElement.getElementsByTagName(ResourceTagNameConst.URI)
                            .item(0).getTextContent());
                    resources.add(resource);
                }
            }
            return resources;
        } catch (Exception e) {
            LogUtility.log(e.getMessage());
            getContext().addMessage(null, new FacesMessage(e.getMessage()));
            return null;
        }
    }

    /**
     * Returns resource based on provided URI
     *
     * @param uri URI of the resource
     * @return
     */
    public ResourceDescription getResource(String uri) {
        try {
            if (StringUtility.isEmpty(uri)) {
                return null;
            }

            if (uri != null && uri.length() > 0 && !uri.startsWith("/")) {
                uri = "/" + uri;
            }

            WebTarget target = getClient().target(baseServerUrl + "/rest_v2/resources" + uri);
            Response response = target.request(APP_XML).get();

            LogUtility.log("JasperReport Resource URI: " + baseServerUrl + "/rest_v2/resources" + uri);
            LogUtility.log("JasperReport Resource URI response:" + response.getStatus());

            if (response.getStatus() != 200) {
                getContext().addMessage(null, new FacesMessage(
                        String.format(msgProvider.getErrorMessage(ErrorKeys.REPORTS_FAILED_TO_GET_RESOURCE),
                                Integer.toString(response.getStatus()))));
                return null;
            }

            String xmlString = response.readEntity(String.class);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlString.getBytes(UTF8));
            Document doc = builder.parse(input);
            doc.getDocumentElement().normalize();
            Element root = doc.getDocumentElement();

            ResourceDescription resource = new ResourceDescription();
            resource.setDescription(root.getElementsByTagName(ResourceTagNameConst.DESCRIPTION)
                    .item(0).getTextContent());
            resource.setLabel(root.getElementsByTagName(ResourceTagNameConst.LABEL)
                    .item(0).getTextContent());
            resource.setType(root.getTagName());
            resource.setUri(root.getElementsByTagName(ResourceTagNameConst.URI)
                    .item(0).getTextContent());
            return resource;
        } catch (Exception e) {
            LogUtility.log(e.getMessage());
            getContext().addMessage(null, new FacesMessage(e.getMessage()));
            return null;
        }
    }

    /**
     * Returns report description based on provided URI
     *
     * @param uri URI of the report
     * @return
     */
    public ResourceDescription getReport(String uri) {
        ResourceDescription report = getResource(uri);
        if (report != null && report.getType().equalsIgnoreCase(ResourceTypeConst.REPORT_UNIT)) {
            return report;
        }
        return null;
    }

    /**
     * Returns list of report parameters related to provided report
     *
     * @param reportPath Path to report on the reports server
     * @return
     */
    public List<ReportParam> getReportParameters(String reportPath) {
        try {
            if (reportPath != null && reportPath.length() > 0 && !reportPath.startsWith("/")) {
                reportPath = "/" + reportPath;
            }

            WebTarget target = getClient().target(baseServerUrl + "/rest_v2/reports" + reportPath + "/inputControls");
            Response response = target.request(APP_XML).get();

            // if no parameters
            if (response.getStatus() == 204) {
                return null;
            }

            if (response.getStatus() != 200) {
                getContext().addMessage(null, new FacesMessage(
                        String.format(msgProvider.getErrorMessage(ErrorKeys.REPORTS_FAILED_TO_GET_PARAMS),
                                Integer.toString(response.getStatus()))));
                return null;
            }

            String xmlString = response.readEntity(String.class);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlString.getBytes(UTF8));
            Document doc = builder.parse(input);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName(ResourceTagNameConst.INPUT_CONTROL);
            List<ReportParam> params = new ArrayList<>();

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                Element eElement = (Element) nNode;
                ReportParam param = new ReportParam();
                param.setId(eElement.getElementsByTagName(ResourceTagNameConst.ID)
                        .item(0).getTextContent());
                NodeList descriptions = eElement.getElementsByTagName(ResourceTagNameConst.DESCRIPTION);
                if (descriptions != null && descriptions.getLength() > 0) {
                    param.setDescription(descriptions.item(0).getTextContent());
                }
                param.setLabel(eElement.getElementsByTagName(ResourceTagNameConst.LABEL)
                        .item(0).getTextContent());
                param.setMandatory(Boolean.parseBoolean(eElement.getElementsByTagName(ResourceTagNameConst.MANDATORY)
                        .item(0).getTextContent()));
                param.setReadOnly(Boolean.parseBoolean(eElement.getElementsByTagName(ResourceTagNameConst.READ_ONLY)
                        .item(0).getTextContent()));
                param.setVisible(Boolean.parseBoolean(eElement.getElementsByTagName(ResourceTagNameConst.VISIBLE)
                        .item(0).getTextContent()));
                param.setType(eElement.getElementsByTagName(ResourceTagNameConst.TYPE)
                        .item(0).getTextContent());
                param.setUri(eElement.getElementsByTagName(ResourceTagNameConst.URI)
                        .item(0).getTextContent());
                Element state = (Element) eElement.getElementsByTagName(ResourceTagNameConst.STATE).item(0);

                // Check for options
                Element optionsElement = (Element) state.getElementsByTagName(ResourceTagNameConst.OPTIONS).item(0);
                if (optionsElement != null) {
                    NodeList options = state.getElementsByTagName(ResourceTagNameConst.OPTION);
                    for (int i = 0; i < options.getLength(); i++) {
                        Element optionElement = (Element) options.item(i);
                        ReportParamOption option = new ReportParamOption();
                        option.setLabel(optionElement.getElementsByTagName(ResourceTagNameConst.LABEL)
                                .item(0).getTextContent());
                        option.setValue(optionElement.getElementsByTagName(ResourceTagNameConst.VALUE)
                                .item(0).getTextContent());
                        option.setSelected(Boolean.parseBoolean(optionElement.getElementsByTagName(ResourceTagNameConst.SELECTED)
                                .item(0).getTextContent()));
                        param.getOptions().add(option);
                    }
                }
                params.add(param);
            }
            return params;
        } catch (Exception e) {
            LogUtility.log(e.getMessage());
            getContext().addMessage(null, new FacesMessage(e.getMessage()));
            return null;
        }
    }

    public String makeReportUrl(String reportUrl, ReportParam[] params, String format) {
        try {
            if (reportUrl != null && reportUrl.length() > 0 && !reportUrl.startsWith("/")) {
                reportUrl = "/" + reportUrl;
            }
//            reportUrl = baseServerUrl + "/rest_v2/reports" + reportUrl + "." + format + "?lang="
//                    + langBean.getLocale() + "&ReportLocale=" + langBean.getLocale().replace("-", "_");
            reportUrl = baseServerUrl + "/rest_v2/reports" + reportUrl + "." + format + "?lang="
                    + langBean.getLocale() + "&REPORT_LOCALE=" + langBean.getLocale().replace("-", "_");
            String error = "";

            if (params != null && params.length > 0) {
                for (ReportParam param : params) {
                    // Check parameter mandatory
                    if (param.isMandatory() && !param.getType().equalsIgnoreCase(ParamTypeConst.BOOL)) {
                        if (param.getType().equalsIgnoreCase(ParamTypeConst.MULTI_SELECT)
                                || param.getType().equalsIgnoreCase(ParamTypeConst.MULTI_SELECT_CHECKBOX)) {
                            if (param.getSelectedOptions() == null || param.getSelectedOptions().length <= 0) {
                                error += String.format(
                                        msgProvider.getErrorMessage(ErrorKeys.REPORTS_FILL_IN_PARAM),
                                        param.getLabel()) + ";";
                                getContext().addMessage(null, new FacesMessage(
                                        String.format(msgProvider.getErrorMessage(ErrorKeys.REPORTS_FILL_IN_PARAM),
                                                param.getLabel()) + ";"));
                            }
                        } else {
                            if (param.getValueString() == null || param.getValueString().equals("")
                                    || param.getValueString().equalsIgnoreCase("~NOTHING~")) {
                                error += String.format(
                                        msgProvider.getErrorMessage(ErrorKeys.REPORTS_FILL_IN_PARAM),
                                        param.getLabel()) + ";";
                                getContext().addMessage(null, new FacesMessage(
                                        String.format(msgProvider.getErrorMessage(ErrorKeys.REPORTS_FILL_IN_PARAM),
                                                param.getLabel()) + ";"));
                            }
                        }
                    }

                    if (param.getType().equalsIgnoreCase(ParamTypeConst.BOOL)) {
                        reportUrl += "&" + param.getId() + "=" + Boolean.toString(param.isValueBoolean());
                    }
                    if (param.getType().equalsIgnoreCase(ParamTypeConst.MULTI_SELECT)
                            || param.getType().equalsIgnoreCase(ParamTypeConst.MULTI_SELECT_CHECKBOX)) {
                        if (param.getSelectedOptions() != null && param.getSelectedOptions().length > 0) {
                            for (String option : param.getSelectedOptions()) {
                                reportUrl += "&" + param.getId() + "=" + option;
                            }
                        }
                    }
                    if (param.getType().equalsIgnoreCase(ParamTypeConst.SINGLE_SELECT)
                            || param.getType().equalsIgnoreCase(ParamTypeConst.SINGLE_SELECT_RADIO)) {
                        if (param.getValueString() != null && !param.getValueString().equalsIgnoreCase("~NOTHING~")) {
                            reportUrl += "&" + param.getId() + "=" + param.getValueString();
                        }
                    }
                    if (param.getType().equalsIgnoreCase(ParamTypeConst.SINGLE_VALUE_DATE)
                            || param.getType().equalsIgnoreCase(ParamTypeConst.SINGLE_VALUE_DATE_TIME)) {
                        if (param.getValueString() != null && !param.getValueString().equals("")) {
                            SimpleDateFormat initFormat = new SimpleDateFormat("dd/MM/yy");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                Date date;
                                // Validate date
                                date = initFormat.parse(param.getValueString());
                                reportUrl += "&" + param.getId() + "=" + sdf.format(date);
                            } catch (Exception e) {
                                error += String.format(
                                        msgProvider.getErrorMessage(ErrorKeys.REPORTS_PARAM_IS_NOT_DATE),
                                        param.getLabel()) + ";";
                                getContext().addMessage(null, new FacesMessage(
                                        String.format(msgProvider.getErrorMessage(ErrorKeys.REPORTS_PARAM_IS_NOT_DATE),
                                                param.getLabel()) + ";"));
                            }
                        }
                    }
                    if (param.getType().equalsIgnoreCase(ParamTypeConst.SINGLE_VALUE_NUMBER) || param.getType().equalsIgnoreCase(ParamTypeConst.NUMBER)) {
                        if (param.getValueString() != null && !param.getValueString().equals("")) {
                            try {
                                Double dbl;
                                // Validate number
                                dbl = Double.parseDouble(param.getValueString());
                                reportUrl += "&" + param.getId() + "=" + dbl.toString().replace(",", ".");
                            } catch (Exception e) {
                                error += String.format(
                                        msgProvider.getErrorMessage(ErrorKeys.REPORTS_PARAM_IS_NOT_NUMBER),
                                        param.getLabel()) + ";";
                                getContext().addMessage(null, new FacesMessage(
                                        String.format(msgProvider.getErrorMessage(ErrorKeys.REPORTS_PARAM_IS_NOT_NUMBER),
                                                param.getLabel()) + ";"));
                            }
                        }
                    }
                    if (param.getType().equalsIgnoreCase(ParamTypeConst.SINGLE_VALUE_TEXT)
                            || param.getType().equalsIgnoreCase(ParamTypeConst.SINGLE_VALUE)
                            || param.getType().equalsIgnoreCase(ParamTypeConst.SINGLE_VALUE_TIME)
                            || param.getType().equalsIgnoreCase(ParamTypeConst.TEXT)) {
                        if (param.getValueString() != null && !param.getValueString().equals("")) {
                            reportUrl += "&" + param.getId() + "=" + param.getValueString();
                        }
                    }
                }
            }
            if (error.length() > 0) {
                return null;
            }
            return reportUrl;
        } catch (Exception e) {
            LogUtility.log(e.getMessage());
            getContext().addMessage(null, new FacesMessage(e.getMessage()));
            return null;
        }
    }

    public void runReport(String reportUrl, ReportParam[] params, String format) {
        try {
            String fullReportUrl = makeReportUrl(reportUrl, params, format);
            if (StringUtility.isEmpty(fullReportUrl)) {
                return;
            }
            String contentType = "text/html";
            if (format.equalsIgnoreCase("pdf")) {
                contentType = "application/pdf";
            } else if (format.equalsIgnoreCase("docx")) {
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else if (format.equalsIgnoreCase("rtf")) {
                contentType = "application/rtf";
            } else if (format.equalsIgnoreCase("xlsx")) {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }

            LogUtility.log("JasperReport Report URL: " + fullReportUrl);

            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

            response.reset();
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "inline; filename=\"report." + format + "\"");

            try (OutputStream responseOutputStream = response.getOutputStream()) {
                String sessionId = login();
                URL url = new URL(fullReportUrl);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);
                con.setDoOutput(true);
                con.setRequestMethod("GET");
                con.connect();

                try (InputStream inputStream = con.getInputStream()) {
                    byte[] bytesBuffer = new byte[2048];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(bytesBuffer)) > 0) {
                        responseOutputStream.write(bytesBuffer, 0, bytesRead);
                    }
                    responseOutputStream.flush();
                }
            }
            facesContext.responseComplete();

        } catch (Exception e) {
            LogUtility.log(e.getMessage());
            getContext().addMessage(null, new FacesMessage(e.getMessage()));
        }
    }

    /**
     * Logs in to Jasper Reports and returns session id
     *
     * @return
     */
    public String login() {
        URL url;
        HttpURLConnection conn;
        String jsessionId = "";
        String urlLink = baseServerUrl + "/rest_v2/login?j_username=" + user + "&j_password=" + password;
        String sessionId = "";

        try {
            url = new URL(urlLink);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() == 200) {
                Map<String, List<String>> map = conn.getHeaderFields();
                
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    if(!sessionId.equals("")){
                        break;
                    }
                        
                    if (entry.getKey() == null) {
                        continue;
                    }
                    if (entry.getKey().equalsIgnoreCase("Set-Cookie")) {
                        List<String> headerValues = entry.getValue();
                        for(String value: headerValues){
                            if(value.toUpperCase().startsWith("JSESSIONID")){
                                sessionId = value.substring(value.indexOf("=") + 1, value.indexOf(";"));
                                break;
                            }
                        }
                    }
                }
            }
            
            conn.disconnect();
            return sessionId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsessionId;
    }
}
