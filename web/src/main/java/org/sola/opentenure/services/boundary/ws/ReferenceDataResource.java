package org.sola.opentenure.services.boundary.ws;

import java.util.ArrayList;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.sola.cs.services.boundary.transferobjects.claim.FormTemplateTO;
import org.sola.opentenure.services.boundary.beans.AbstractWebRestService;
import org.sola.opentenure.services.boundary.beans.referencedata.ReferenceData;
import org.sola.opentenure.services.boundary.beans.responses.ResponseFactory;
import org.sola.cs.services.ejbs.claim.entities.ClaimStatus;
import org.sola.cs.services.ejbs.claim.businesslogic.ClaimEJBLocal;
import org.sola.cs.services.boundary.transferobjects.referencedata.AdministrativeBoundaryStatusTO;
import org.sola.cs.services.boundary.transferobjects.referencedata.AdministrativeBoundaryTypeTO;
import org.sola.cs.services.boundary.transferobjects.referencedata.ClaimStatusTO;
import org.sola.cs.services.boundary.transferobjects.referencedata.IdTypeTO;
import org.sola.cs.services.boundary.transferobjects.referencedata.LandUseTO;
import org.sola.cs.services.boundary.transferobjects.referencedata.ProjectTO;
import org.sola.cs.services.boundary.transferobjects.referencedata.RrrTypeTO;
import org.sola.cs.services.boundary.transferobjects.referencedata.SourceTypeTO;
import org.sola.cs.services.boundary.transferobjects.system.LanguageTO;
import org.sola.services.common.contracts.CsGenericTranslator;
import org.sola.cs.services.ejb.search.businesslogic.SearchCSEJBLocal;
import org.sola.opentenure.services.boundary.beans.exceptions.ExceptionFactory;

/**
 * Reference data REST Web Service
 *
 */
public class ReferenceDataResource extends AbstractWebRestService {
    @EJB
    ClaimEJBLocal claimEJB;
    
    @EJB
    SearchCSEJBLocal searchEJB;
   
    @Inject
    ReferenceData refData;

    /**
     * Returns List of {@link LanguageTO} objects
     * @param localeCode Locale code used to return localized values
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getlanguages|getLanguages}")
    public String getLanguages(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return getMapper().writeValueAsString(CsGenericTranslator.toTOList(refData.getLanguages(localeCode), LanguageTO.class));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    /**
     * Returns List of {@link ProjectTO} objects
     * @param localeCode Locale code used to return localized values
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getprojects|getProjects}")
    public String getProjects(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return getMapper().writeValueAsString(CsGenericTranslator.toTOList(searchEJB.searchCurrentUserProjectConfig(), ProjectTO.class));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    /**
     * Returns List of {@link RrrTypeTO} objects
     * @param localeCode Locale code used to return localized values
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getlanduses|getLandUses}")
    public String getLandUses(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return getMapper().writeValueAsString(CsGenericTranslator.toTOList(refData.getLandUses(localeCode, true), LandUseTO.class));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    /**
     * Returns List of {@link RrrTypeTO} objects
     * @param localeCode Locale code used to return localized values
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getclaimtypes|getClaimTypes}")
    public String getClaimTypes(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return getMapper().writeValueAsString(CsGenericTranslator.toTOList(refData.getRightTypes(localeCode, true), RrrTypeTO.class));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    /**
     * Returns maximum file sized allowed for uploading to the server
     * @param localeCode Locale code used to return localized values
     * @param projectId Project ID
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getmaxfilesize|getMaxFileSize}/{projectId}")
    public String getMaxFileSize(@PathParam(value = LOCALE_CODE) String localeCode, @PathParam(value = "projectId") String projectId){
        try {
            return ResponseFactory.buildResultResponse(Integer.toString(claimEJB.getMaxFileSize(projectId)));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    /**
     * Returns upload limit in KB, allowed per user per day
     * @param localeCode Locale code used to return localized values
     * @param projectId Project ID
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getuploadlimit|getUploadLimit}/{projectId}")
    public String getUploadLimit(@PathParam(value = LOCALE_CODE) String localeCode, @PathParam(value = "projectId") String projectId){
        try {
            return ResponseFactory.buildResultResponse(Integer.toString(claimEJB.getUploadLimit(projectId)));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    /**
     * Returns List of {@link ClaimStatus} objects
     * @param localeCode Locale code used to return localized values
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getclaimstatuses|getClaimStatuses}")
    public String getClaimStatuses(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return getMapper().writeValueAsString(CsGenericTranslator.toTOList(refData.getClaimStatuses(localeCode), ClaimStatusTO.class));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    /**
     * Returns List of {@link IdTypeTO} objects
     * @param localeCode Locale code used to return localized values
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getdocumenttypes|getDocumentTypes}")
    public String getDocumentTypes(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return getMapper().writeValueAsString(CsGenericTranslator.toTOList(refData.getDocumentTypes(localeCode, true), SourceTypeTO.class));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    /**
     * Returns List of {@link AdministrativeBoundaryTypeTO} objects
     * @param localeCode Locale code used to return localized values
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getadministrativeboundarytypes|getAdministrativeBoundaryTypes}")
    public String getAdministrativeBoundaryTypes(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return getMapper().writeValueAsString(CsGenericTranslator.toTOList(refData.getAdministrativeBoundaryTypes(localeCode, true), AdministrativeBoundaryTypeTO.class));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    /**
     * Returns List of {@link AdministrativeBoundaryStatusTO} objects
     * @param localeCode Locale code used to return localized values
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getadministrativeboundarystatuses|getAdministrativeBoundaryStatuses}")
    public String getAdministrativeBoundaryStatuses(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return getMapper().writeValueAsString(CsGenericTranslator.toTOList(refData.getAdministrativeBoundaryStatuses(localeCode, true), AdministrativeBoundaryStatusTO.class));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    /**
     * Returns List of {@link IdTypeTO} objects
     * @param localeCode Locale code used to return localized values
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getidtypes|getIdTypes}")
    public String getIdTypes(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return getMapper().writeValueAsString(CsGenericTranslator.toTOList(refData.getIdTypes(localeCode, true), IdTypeTO.class));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
        
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getdefaultformtemplate|getDefaultFormTemplate}")
    /**
     * Returns default dynamic form template
     */
    public String getDefaultFormTemplate(@PathParam(value = LOCALE_CODE) String localeCode) {
        try {
            FormTemplateTO formTempl = CsGenericTranslator.toTO(
                    claimEJB.getDefaultFormTemplate(localeCode), FormTemplateTO.class);

            if (formTempl != null) {
                return getMapper().writeValueAsString(formTempl);
            }
            return "{}";
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getformtemplate|getFormTemplate}")
    /**
     * Returns dynamic form template by name
     */
    public String getFormTemplate(@PathParam(value = LOCALE_CODE) String localeCode,
            @QueryParam(value = "name") String name) {
        try {
            FormTemplateTO formTempl = CsGenericTranslator.toTO(claimEJB.getFormTemplate(name, localeCode), FormTemplateTO.class);

            if (formTempl == null) {
                throw ExceptionFactory.buildNotFound(localeCode);
            }
            return getMapper().writeValueAsString(formTempl);
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getformtemplates|getFormTemplates}")
    /**
     * Returns dynamic form templates
     */
    public String getFormTemplates(@PathParam(value = LOCALE_CODE) String localeCode) {
        try {
            List<FormTemplateTO> formTemplates = CsGenericTranslator.toTOList(claimEJB.getFormTemplates(localeCode), FormTemplateTO.class);

            if (formTemplates == null) {
                return getMapper().writeValueAsString(new ArrayList<>());
                //throw ExceptionFactory.buildNotFound(localeCode);
            }
            return getMapper().writeValueAsString(formTemplates);
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
}
