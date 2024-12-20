package org.sola.opentenure.services.boundary.ws;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Set;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

@ApplicationPath("ws")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        resources.add(MultiPartFeature.class);
        resources.add(org.sola.opentenure.services.boundary.ws.ProtocolVersionFilter.class);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(org.sola.opentenure.services.boundary.ws.ClaimResource.class);
        resources.add(org.sola.opentenure.services.boundary.ws.DynamicFormsResource.class);
        resources.add(org.sola.opentenure.services.boundary.ws.LoginResource.class);
        resources.add(org.sola.opentenure.services.boundary.ws.ReferenceDataResource.class);
        resources.add(org.sola.opentenure.services.boundary.ws.ReferenceDataResourceLocalized.class);
        resources.add(org.sola.opentenure.services.boundary.ws.ReferenceDataResourceUnlocalized.class);
    }
    
}