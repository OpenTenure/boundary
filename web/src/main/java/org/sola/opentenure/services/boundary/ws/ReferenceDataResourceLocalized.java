package org.sola.opentenure.services.boundary.ws;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;

@Path("{localeCode:[a-zA-Z]{2}[-]{1}[a-zA-Z]{2}}/ref")
@RequestScoped
/** Wrapper class of {@link ReferenceDataResource} to return localized reference data lists */
public class ReferenceDataResourceLocalized extends ReferenceDataResource {
    public ReferenceDataResourceLocalized(){
        super();
    }
}
