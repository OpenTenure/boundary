package org.sola.opentenure.services.boundary.ws;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.Path;

@Path("/ref")
@RequestScoped
/** Wrapper class of {@link ReferenceDataResource} to return unlocalized reference data lists */
public class ReferenceDataResourceUnlocalized extends ReferenceDataResource {
    public ReferenceDataResourceUnlocalized(){
        super();
    }
}
