package org.sola.opentenure.services.boundary.ws;

import java.io.IOException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

/**
 * Filter class to add protocol version number into every response
 */
public class ProtocolVersionFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        responseContext.getHeaders().add("ProtoVersion", "1.2");
    }
}
