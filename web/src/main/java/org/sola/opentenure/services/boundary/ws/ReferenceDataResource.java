package org.sola.opentenure.services.boundary.ws;

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.sola.cs.services.boundary.transferobjects.claim.FormTemplateTO;
import org.sola.opentenure.services.boundary.beans.AbstractWebRestService;
import org.sola.opentenure.services.boundary.beans.referencedata.ReferenceData;
import org.sola.opentenure.services.boundary.beans.responses.ResponseFactory;
import org.sola.cs.services.ejbs.claim.entities.ClaimStatus;
import org.sola.cs.services.ejbs.claim.businesslogic.ClaimEJBLocal;
import org.sola.cs.services.boundary.transferobjects.configuration.CrsTO;
import org.sola.cs.services.boundary.transferobjects.referencedata.ClaimStatusTO;
import org.sola.cs.services.boundary.transferobjects.referencedata.IdTypeTO;
import org.sola.cs.services.boundary.transferobjects.referencedata.LandUseTO;
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
     * Creates a new instance of TestResource
     */
    public ReferenceDataResource() {
    }

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
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getmaxfilesize|getMaxFileSize}")
    public String getMaxFileSize(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return ResponseFactory.buildResultResponse(Integer.toString(claimEJB.getMaxFileSize()));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    /**
     * Returns upload limit in KB, allowed per user per day
     * @param localeCode Locale code used to return localized values
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getuploadlimit|getUploadLimit}")
    public String getUploadLimit(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return ResponseFactory.buildResultResponse(Integer.toString(claimEJB.getUploadLimit()));
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
    
    /**
     * Returns list of CRS allowed on the server
     * @param localeCode Locale code used to return localized values
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getcrs|getCrs}")
    public String getCrs(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return getMapper().writeValueAsString(CsGenericTranslator.toTOList(searchEJB.getCrsList(), CrsTO.class));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
     /**
     * Returns community area, defining where claims can be submitted. Returns in WKT format
     * @param localeCode Locale code used to return localized values
     * @return
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getcommunityarea|getCommunityArea}")
    public String getCommunityArea(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return ResponseFactory.buildResultResponse(refData.getCommunityArea());
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getmapextent|getMapExtent}")
    public String getMapExtent(@PathParam(value = LOCALE_CODE) String localeCode){
        try {
            return getMapper().writeValueAsString(refData.getMapExtent());
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
                throw ExceptionFactory.buildNotFound(localeCode);
            }
            return getMapper().writeValueAsString(formTemplates);
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
}
