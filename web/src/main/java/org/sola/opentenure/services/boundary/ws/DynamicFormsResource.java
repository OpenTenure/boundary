package org.sola.opentenure.services.boundary.ws;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.sola.opentenure.services.boundary.beans.AbstractWebRestService;
import org.sola.opentenure.services.boundary.beans.exceptions.ExceptionFactory;
import org.sola.cs.services.ejbs.claim.businesslogic.ClaimEJBLocal;
import org.sola.cs.services.boundary.transferobjects.claim.FormTemplateTO;
import org.sola.services.common.contracts.CsGenericTranslator;

@Path("/claim")
@RequestScoped
public class DynamicFormsResource extends AbstractWebRestService {
       
    @EJB
    ClaimEJBLocal claimEjb;
    
    public DynamicFormsResource(){
        super();
    }
    
    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getdefaultformtemplate|getDefaultFormTemplate}")
    /** 
     * Returns default dynamic form template
     */
    public String getDefaultFormTemplate() {
        try {
            FormTemplateTO formTempl = CsGenericTranslator.toTO(claimEjb.getDefaultFormTemplate(null), FormTemplateTO.class);
            
            if (formTempl != null){
                return getMapper().writeValueAsString(formTempl);
            }
            return "{}";
        } catch (Exception e) {
            throw processException(e, null);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getformtemplate|getFormTemplate}")
    /** 
     * Returns dynamic form template by name 
     */
    public String getFormTemplate(@QueryParam(value = "name") String name) {
        try {
            FormTemplateTO formTempl = CsGenericTranslator.toTO(claimEjb.getFormTemplate(name, null), FormTemplateTO.class);
            
            if (formTempl == null){
                throw ExceptionFactory.buildNotFound(null);
            }
            return getMapper().writeValueAsString(formTempl);
        } catch (Exception e) {
            throw processException(e, null);
        }
    }
}
