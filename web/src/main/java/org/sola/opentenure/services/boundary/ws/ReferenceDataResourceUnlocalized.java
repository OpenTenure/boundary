package org.sola.opentenure.services.boundary.ws;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;

@Path("/ref")
@RequestScoped
/** Wrapper class of {@link ReferenceDataResource} to return unlocalized reference data lists */
public class ReferenceDataResourceUnlocalized extends ReferenceDataResource {
    public ReferenceDataResourceUnlocalized(){
        super();
    }
}
