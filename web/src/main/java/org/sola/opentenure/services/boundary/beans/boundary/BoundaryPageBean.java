package org.sola.opentenure.services.boundary.beans.boundary;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.sola.common.ConfigConstants;
import org.sola.common.RolesConstants;
import org.sola.common.StringUtility;
import org.sola.cs.services.ejb.refdata.entities.AdministrativeBoundaryStatus;
import org.sola.cs.services.ejb.refdata.entities.AdministrativeBoundaryType;
import org.sola.cs.services.ejb.search.businesslogic.SearchCSEJBLocal;
import org.sola.cs.services.ejb.search.repository.entities.AdministrativeBoundarySearchResult;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.sola.cs.services.ejbs.admin.businesslogic.AdminCSEJBLocal;
import org.sola.cs.services.ejbs.claim.businesslogic.ClaimEJBLocal;
import org.sola.cs.services.ejbs.claim.entities.AdministrativeBoundary;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import org.sola.opentenure.services.boundary.beans.exceptions.OTWebException;
import org.sola.opentenure.services.boundary.beans.helpers.AdministrativeBoundaryTypeSorter;
import org.sola.opentenure.services.boundary.beans.helpers.ErrorKeys;
import org.sola.opentenure.services.boundary.beans.helpers.MessageBean;
import org.sola.opentenure.services.boundary.beans.helpers.MessageProvider;
import org.sola.opentenure.services.boundary.beans.helpers.MessagesKeys;
import org.sola.opentenure.services.boundary.beans.language.LanguageBean;
import org.sola.opentenure.services.boundary.beans.referencedata.ReferenceData;
import org.sola.services.common.LocalInfo;
import org.sola.services.common.logging.LogUtility;

/**
 * Provides method and listeners for boundary page
 */
@Named
@ViewScoped
public class BoundaryPageBean extends AbstractBackingBean {

    @EJB
    SearchCSEJBLocal searchEjb;

    @EJB
    AdminCSEJBLocal adminEjb;
    
    @EJB
    ClaimEJBLocal claimEjb;

    @Inject
    ReferenceData refData;

    @Inject
    MessageProvider msgProvider;

    @EJB
    SystemCSEJBLocal systemEjb;
    
    @Inject
    MessageBean msg;

    @Inject
    LanguageBean langBean;

    private final String ACTION_SAVED = "saved";
    private final String ACTION_APPROVED = "approved";
    private final String ACTION_DELETED = "deleted";

    private String id;
    private AdministrativeBoundary boundary;
    private String recorderName;
    private AdministrativeBoundarySearchResult[] allBoundaries;
    private AdministrativeBoundarySearchResult[] parentBoundaries;
    private AdministrativeBoundarySearchResult[] allBoundariesFormatted;
    private AdministrativeBoundarySearchResult[] approvedBoundariesFormatted;
    public BoundaryPageBean() {
        super();
    }

    @PostConstruct
    private void init() {
        id = getRequestParam("id");
        String action = getRequestParam("action");

        if (!StringUtility.isEmpty(action)) {
            if (action.equalsIgnoreCase(ACTION_SAVED)) {
                msg.setSuccessMessage(msgProvider.getMessage(MessagesKeys.BOUNDARY_PAGE_SAVED));
            }
            if (action.equalsIgnoreCase(ACTION_APPROVED)) {
                msg.setSuccessMessage(msgProvider.getMessage(MessagesKeys.BOUNDARY_PAGE_APPROVED));
            }
            if (action.equalsIgnoreCase(ACTION_DELETED)) {
                msg.setSuccessMessage(msgProvider.getMessage(MessagesKeys.BOUNDARY_PAGE_DELETED));
            }
        }

        if (!StringUtility.isEmpty(id)) {
            if (boundary == null) {
                boundary = claimEjb.getAdministrativeBoundary(id);
            }
        } else {
            boundary = new AdministrativeBoundary();
            boundary.setId(UUID.randomUUID().toString());
            boundary.setTypeCode(getBoundaryTypeCodeByParentId(null));
            boundary.setStatusCode(AdministrativeBoundaryStatus.STATUS_PENDING);
        }
    }

    public AdministrativeBoundarySearchResult[] getAllBoundariesFormatted(boolean addDummy) {
        if (allBoundariesFormatted == null) {
            allBoundariesFormatted = getFormattedBoundaries(searchEjb.searchAllAdministrativeBoundaries(langBean.getLocale()), addDummy);
        }
        return allBoundariesFormatted;
    }
    
    public AdministrativeBoundarySearchResult[] getApprovedBoundariesFormatted(boolean addDummy) {
        if (approvedBoundariesFormatted == null) {
            approvedBoundariesFormatted = getFormattedBoundaries(searchEjb.searchApprovedAdministrativeBoundaries(langBean.getLocale()), addDummy);
        }
        return approvedBoundariesFormatted;
    }

    public AdministrativeBoundarySearchResult[] getBoundaries() {
        if (allBoundaries == null) {
            List<AdministrativeBoundarySearchResult> boundaries = searchEjb.searchAllAdministrativeBoundaries(langBean.getLocale());
            if (boundaries != null) {
                allBoundaries = boundaries.toArray(new AdministrativeBoundarySearchResult[boundaries.size()]);
            } else {
                allBoundaries = new AdministrativeBoundarySearchResult[0];
            }
        }
        return allBoundaries;
    }

    public AdministrativeBoundarySearchResult[] getParentBoundaries() {
        if (parentBoundaries == null) {
            parentBoundaries = getFormattedBoundaries(searchEjb.searchParentAdministrativeBoundaries(langBean.getLocale()), true);
        }
        return parentBoundaries;
    }

    private AdministrativeBoundarySearchResult[] getFormattedBoundaries(List<AdministrativeBoundarySearchResult> boundaries, boolean addDummy) {
        if (boundaries != null) {
            // Adjust names with padding based on level
            for (AdministrativeBoundarySearchResult r : boundaries) {
                if (r.getLevel() > 1) {
                    String padding = "";
                    for (int i = 1; i < r.getLevel(); i++) {
                        padding += "&nbsp;&nbsp;";
                    }
                    r.setName(padding + r.getName());
                }
            }
            if (addDummy) {
                AdministrativeBoundarySearchResult dummy = new AdministrativeBoundarySearchResult();
                dummy.setId("");
                dummy.setName("");
                boundaries.add(0, dummy);
            }
            return boundaries.toArray(new AdministrativeBoundarySearchResult[boundaries.size()]);
        } else {
            return new AdministrativeBoundarySearchResult[0];
        }
    }

    public String getRecorderName() {
        if (StringUtility.isEmpty(recorderName) && boundary != null) {
            recorderName = getFullUserName(boundary.getRecorderName());
            if(StringUtility.isEmpty(recorderName)){
                recorderName = StringUtility.empty(boundary.getRecorderName());
            }
        }
        return recorderName;
    }
    
    public String getFullUserName(String userName) {
        if (!StringUtility.isEmpty(userName)) {
            return adminEjb.getUserFullName(userName);
        }
        return "";
    }
    
    public String getFullParentNames() {
        if (StringUtility.isEmpty(boundary.getParentId())) {
            return "";
        }

        AdministrativeBoundarySearchResult[] boundaries = getParentBoundaries();
        if (boundaries != null) {
            for (AdministrativeBoundarySearchResult b : boundaries) {
                if (b.getId().equalsIgnoreCase(boundary.getParentId())) {
                    return b.getName();
                }
            }
        }
        return "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHeader() {
        String name;
        if (boundary != null && !StringUtility.isEmpty(boundary.getName())) {
            name = String.format(msgProvider.getMessage(MessagesKeys.BOUNDARY_PAGE_TITLE), boundary.getName(), getBoundaryTypeName());
        } else {
            name = msgProvider.getMessage(MessagesKeys.BOUNDARY_PAGE_TITLE_NEW);
        }
        return name;
    }

    public String getBoundaryPrintCrsDescription(){
        return systemEjb.getSetting(ConfigConstants.BOUNDARY_PRINT_CRS_DESCRIPTION, "");
    }
    
    public String getBoundaryPrintProducedBy(){
        return systemEjb.getSetting(ConfigConstants.BOUNDARY_PRINT_PRODUCED_BY, "");
    }
    
    public String getMonthAndYear(){
        Format formatter = new SimpleDateFormat("MMMM", new Locale(langBean.getLocaleCodeForBundle())); 
        String s = formatter.format(new Date());
        s = s.substring(0, 1).toUpperCase() + s.substring(1);
        Calendar cal = Calendar.getInstance();
        
        return s + " " + cal.get(Calendar.YEAR);
    }
    
    public String getBoundaryPrintLocationDescription(){
        String location = "";
        String countryName = systemEjb.getSetting(ConfigConstants.BOUNDARY_PRINT_COUNTRY_NAME, "");
        
        if (boundary != null && !StringUtility.isEmpty(boundary.getName())) {
            location = String.format(msgProvider.getMessage(MessagesKeys.BOUNDARY_PAGE_TITLE), boundary.getName(), getBoundaryTypeName());
            List<AdministrativeBoundarySearchResult> parents = searchEjb.searchParentAdministrativeBoundaries(boundary.getId(), langBean.getLocale());
            if(parents != null && parents.size() > 0){
                for (int i = 0; i < parents.size() - 1; i++) {
                    location += ", " + parents.get(i).getName() + " " + parents.get(i).getTypeName();
                }
            }
        }
        if(!StringUtility.isEmpty(countryName)){
            location += ", " + countryName;
        }
        
        return String.format(msgProvider.getMessage(MessagesKeys.BOUNDARY_PRINT_PAGE_LOCATION_OF), location);
    }
    
    public AdministrativeBoundary getBoundary() {
        return boundary;
    }

    public String cleanPath(String path) {
        if (StringUtility.isEmpty(path)) {
            return "";
        }
        return path.replace("{", "").replace("}", "").replace(",", "-");
    }

    public AdministrativeBoundaryType[] getBoundaryTypes() {
        return refData.getAdministrativeBoundaryTypes(langBean.getLocale(), true, true);
    }

    public String getBoundaryTypeCodeByParentId(String parentId) {
        AdministrativeBoundaryType[] boundaryTypes = refData.getAdministrativeBoundaryTypes(langBean.getLocale(), true, false);
        if (boundaryTypes == null || boundaryTypes.length < 1) {
            return null;
        }

        Arrays.sort(boundaryTypes, new AdministrativeBoundaryTypeSorter());

        if (StringUtility.isEmpty(parentId)) {
            return boundaryTypes[0].getCode();
        }

        // Look for parent
        AdministrativeBoundarySearchResult[] boundaries = getParentBoundaries();
        if (boundaries == null || boundaries.length < 1) {
            return null;
        }

        String parentTypeCode = null;

        for (AdministrativeBoundarySearchResult b : boundaries) {
            if (!StringUtility.isEmpty(b.getId()) && b.getId().equals(parentId)) {
                parentTypeCode = b.getTypeCode();
                break;
            }
        }

        if (StringUtility.isEmpty(parentTypeCode)) {
            return null;
        }

        for (int i = 0; i < boundaryTypes.length; i++) {
            if (boundaryTypes[i].getCode().equals(parentTypeCode)) {
                if (i < boundaryTypes.length - 1) {
                    // Return next level
                    return boundaryTypes[i + 1].getCode();
                } else {
                    // Return current level
                    return boundaryTypes[i].getCode();
                }
            }
        }
        return null;
    }

    public void setBoundaryTypeCode() {
        boundary.setTypeCode(getBoundaryTypeCodeByParentId(boundary.getParentId()));
    }

    public String getBoundaryTypeName() {
        AdministrativeBoundaryType[] boundaryTypes = refData.getAdministrativeBoundaryTypes(langBean.getLocale(), true, false);
        if (boundaryTypes == null || boundaryTypes.length < 1) {
            return "";
        }
        if (!StringUtility.isEmpty(boundary.getTypeCode())) {
            for (AdministrativeBoundaryType type : boundaryTypes) {
                if (boundary.getTypeCode().equals(type.getCode())) {
                    return type.getDisplayValue();
                }
            }
        }
        return "";
    }

    public String getBoundaryStatusName() {
        List<AdministrativeBoundaryStatus> boundaryStatuses = refData.getAdministrativeBoundaryStatuses(langBean.getLocale(), true);
        if (boundaryStatuses == null || boundaryStatuses.size() < 1) {
            return "";
        }
        if (!StringUtility.isEmpty(boundary.getStatusCode())) {
            for (AdministrativeBoundaryStatus status : boundaryStatuses) {
                if (boundary.getStatusCode().equals(status.getCode())) {
                    return status.getDisplayValue();
                }
            }
        }
        return "";
    }

    public boolean getCanEdit() {
        return (StringUtility.isEmpty(boundary.getStatusCode()) || boundary.getStatusCode().equalsIgnoreCase(AdministrativeBoundaryStatus.STATUS_PENDING)) && isInRole(RolesConstants.CS_RECORD_CLAIM);
    }

    public boolean getCanApprove() {
        return (boundary.getStatusCode().equalsIgnoreCase(AdministrativeBoundaryStatus.STATUS_PENDING) && isInRole(RolesConstants.CS_MODERATE_CLAIM));
    }

    public void checkCanEdit() throws Exception {
        if (!getCanEdit()) {
            throw new OTWebException(msgProvider.getErrorMessage(ErrorKeys.BOUNDARY_EDIT_NOT_ALLOWED));
        }
    }

    public void save() {
        try {
            runUpdate(new Runnable() {
                @Override
                public void run() {
                    LocalInfo.setBaseUrl(getApplicationUrl());
                    boundary = claimEjb.saveAdministrativeBoundary(boundary);
                }
            });

            redirectWithAction(ACTION_SAVED);
        } catch (Exception e) {
            getContext().addMessage(null, new FacesMessage(processException(e, langBean.getLocale()).getMessage()));
        }
    }

    public void approve() {
        try {
            if (!StringUtility.isEmpty(id) && getCanApprove()) {
                runUpdate(new Runnable() {
                    @Override
                    public void run() {
                        LocalInfo.setBaseUrl(getApplicationUrl());
                        claimEjb.approveAdministrativeBoundary(id);
                    }
                });
                redirectWithAction(ACTION_APPROVED);
            }
        } catch (Exception e) {
            getContext().addMessage(null, new FacesMessage(processException(e, langBean.getLocale()).getMessage()));
        }
    }

    public void delete() {
        try {
            if (!StringUtility.isEmpty(id) && getCanEdit()) {
                runUpdate(new Runnable() {
                    @Override
                    public void run() {
                        LocalInfo.setBaseUrl(getApplicationUrl());
                        claimEjb.deleteAdministrativeBoundary(id);
                    }
                });

                // Redirect
                try {
                    getContext().getExternalContext().redirect(getRequest().getContextPath() + "/boundary/Boundaries.xhtml?action=" + ACTION_DELETED);
                } catch (Exception e) {
                    LogUtility.log("Failed to redirect", e);
                }
            }
        } catch (Exception e) {
            getContext().addMessage(null, new FacesMessage(processException(e, langBean.getLocale()).getMessage()));
        }
    }

    private void redirectWithAction(String action) {
        try {
            getContext().getExternalContext().redirect(getRequest().getContextPath() + "/boundary/ViewBoundary.xhtml?id=" + boundary.getId() + "&action=" + action);
        } catch (Exception e) {
            LogUtility.log("Failed to redirect", e);
        }
    }
}
