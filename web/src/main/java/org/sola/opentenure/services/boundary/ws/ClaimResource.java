package org.sola.opentenure.services.boundary.ws;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.sanselan.util.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.sola.common.ClaimStatusConstants;
import org.sola.common.ConfigConstants;
import org.sola.common.RolesConstants;
import org.sola.common.StringUtility;
import org.sola.common.logging.LogUtility;
import org.sola.cs.common.messaging.MessageUtility;
import org.sola.cs.common.messaging.ServiceMessage;
import org.sola.cs.services.boundary.transferobjects.claim.AdministrativeBoundaryTO;
import org.sola.opentenure.services.boundary.beans.AbstractWebRestService;
import org.sola.opentenure.services.boundary.beans.exceptions.ExceptionFactory;
import org.sola.opentenure.services.boundary.beans.responses.ResponseFactory;
import org.sola.cs.services.ejbs.claim.businesslogic.ClaimEJBLocal;
import org.sola.cs.services.ejbs.claim.entities.AttachmentBinary;
import org.sola.cs.services.ejbs.claim.entities.AttachmentChunk;
import org.sola.cs.services.ejbs.claim.entities.Claim;
import org.sola.cs.services.boundary.transferobjects.claim.AttachmentChunkTO;
import org.sola.cs.services.boundary.transferobjects.claim.AttachmentTO;
import org.sola.cs.services.boundary.transferobjects.claim.ClaimPartyTO;
import org.sola.cs.services.boundary.transferobjects.claim.ClaimShareTO;
import org.sola.cs.services.boundary.transferobjects.claim.ClaimTO;
import org.sola.cs.services.boundary.transferobjects.claim.FormTemplateTO;
import org.sola.cs.services.boundary.transferobjects.search.AdministrativeBoundaryWithGeomSearchResultTO;
import org.sola.cs.services.boundary.transferobjects.search.ClaimSearchResultTO;
import org.sola.cs.services.boundary.transferobjects.search.ClaimSpatialSearchResultTO;
import org.sola.cs.services.boundary.transferobjects.search.MapSearchResultTO;
import org.sola.services.common.LocalInfo;
import org.sola.services.common.contracts.CsGenericTranslator;
import org.sola.services.common.faults.OTRestException;
import org.sola.cs.services.ejb.search.businesslogic.SearchCSEJBLocal;
import org.sola.cs.services.ejb.search.repository.entities.ClaimSpatialSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.GeoJsonAdministrativeBoundary;
import org.sola.cs.services.ejb.search.repository.entities.GeoJsonClaim;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.sola.cs.services.ejbs.claim.entities.AdministrativeBoundary;
import org.sola.services.common.faults.OTProjectNotAccessible;

/**
 * Claim REST Web Service
 */
@Path("{localeCode: [a-zA-Z]{2}[-]{1}[a-zA-Z]{2}}/claim")
@RequestScoped
public class ClaimResource extends AbstractWebRestService {

    @EJB
    ClaimEJBLocal claimEjb;

    @EJB
    SearchCSEJBLocal searchEjb;

    @EJB
    SystemCSEJBLocal systemEjb;
    
    private String hiddenString = "";
    private static final String X_REQ_WITH = "x-requested-with";
    private static final String ACA_HEADERS = "Access-Control-Allow-Headers";
    private static final String ACA_ORIGIN = "Access-Control-Allow-Origin";
    
    private final String geoJson = "{"
            + "\"type\": \"FeatureCollection\","
            + "\"crs\": {"
            + "\"type\": \"name\","
            + "\"properties\": {"
            + "\"name\": \"EPSG:3857\""
            + "}"
            + "},"
            + "\"features\": [%s]"
            + "}";

    @PostConstruct
    private void init() {
        LocalInfo.setBaseUrl(getApplicationUrl());
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getclaim|getClaim}/{claimId}")
    public String getClaim(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @PathParam(value = "claimId") String claimId) {
        try {
            ClaimTO claimTO = CsGenericTranslator.toTO(claimEjb.getClaim(claimId, true), ClaimTO.class);
            if (claimTO == null
                    || (StringUtility.empty(claimTO.getStatusCode()).equalsIgnoreCase(ClaimStatusConstants.CREATED)
                    && !StringUtility.empty(claimTO.getRecorderName()).equalsIgnoreCase(claimEjb.getUserName()))) {
                throw ExceptionFactory.buildNotFound(localeCode);
            }
            removeClaimPrivateInfo(claimTO);
            return getMapper().writeValueAsString(claimTO);
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getdefaultformtemplate|getDefaultFormTemplate}")
    @Deprecated
    /**
     * Returns default dynamic form template.
     */
    public String getDefaultFormTemplate(
            @PathParam(value = LOCALE_CODE) String localeCode) {
        try {
            FormTemplateTO formTempl = CsGenericTranslator.toTO(claimEjb.getDefaultFormTemplate(localeCode), FormTemplateTO.class);

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
    @Deprecated
    /**
     * Returns dynamic form template by name
     */
    public String getFormTemplate(@PathParam(value = LOCALE_CODE) String localeCode,
            @QueryParam(value = "name") String name) {
        try {
            FormTemplateTO formTempl = CsGenericTranslator.toTO(claimEjb.getFormTemplate(name, localeCode), FormTemplateTO.class);

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
    @Deprecated
    /**
     * Returns dynamic form templates
     */
    public String getFormTemplates(@PathParam(value = LOCALE_CODE) String localeCode) {
        try {
            List<FormTemplateTO> formTemplates = CsGenericTranslator.toTOList(claimEjb.getFormTemplates(localeCode), FormTemplateTO.class);

            if (formTemplates == null) {
                throw ExceptionFactory.buildNotFound(localeCode);
            }
            return getMapper().writeValueAsString(formTemplates);
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getclaimsbybox|getClaimsByBox}")
    public String getClaimsByBox(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @QueryParam(value = "minx") String minX,
            @QueryParam(value = "miny") String minY,
            @QueryParam(value = "maxx") String maxX,
            @QueryParam(value = "maxy") String maxY,
            @QueryParam(value = "limit") String limit,
            @QueryParam(value = "projectId") String projectId) {
        try {
            ClaimSpatialSearchParams params = new ClaimSpatialSearchParams();

            int limitInt = 100;
            if (!StringUtility.isEmpty(limit)) {
                limitInt = Integer.parseInt(limit);
            }

            if (StringUtility.isEmpty(minX) || StringUtility.isEmpty(minY)
                    || StringUtility.isEmpty(maxY) || StringUtility.isEmpty(maxX)) {
                throw ExceptionFactory.buildGeneralException(ServiceMessage.OT_WS_EMPTY_REQUIRED_FIELDS, localeCode);
            }

            params.setMinX(minX);
            params.setMinY(minY);
            params.setMaxX(maxX);
            params.setMaxY(maxY);
            params.setProjectId(projectId);
            params.setLimit(limitInt);

            List<ClaimSpatialSearchResultTO> claimTOList = CsGenericTranslator
                    .toTOList(searchEjb.getClaimsByBox(params), ClaimSpatialSearchResultTO.class);
            if (claimTOList == null) {
                return getMapper().writeValueAsString(new ArrayList<>());
            }
            return getMapper().writeValueAsString(claimTOList);
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:addclaimattachment|addClaimAttachment}")
    public String addClaimAttachment(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @QueryParam(value = "claimId") String claimId,
            @QueryParam(value = "attachmentId") String attachmentId) {
        try {
            claimEjb.addClaimAttachment(claimId, attachmentId);
            return ResponseFactory.buildOk();
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getclaimbypoint|getClaimByPoint}")
    public String getClaimByPoint(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @QueryParam(value = "x") String x,
            @QueryParam(value = "y") String y,
            @QueryParam(value = "projectId") String projectId,
            @Context HttpServletResponse response) {
        try {
            if (StringUtility.isEmpty(x) || StringUtility.isEmpty(y)) {
                throw ExceptionFactory.buildGeneralException(ServiceMessage.OT_WS_EMPTY_REQUIRED_FIELDS, localeCode);
            }

            ClaimSearchResultTO claimTO = CsGenericTranslator
                    .toTO(searchEjb.getClaimByCoordinates(x, y, projectId, localeCode), ClaimSearchResultTO.class);
            String result = "";
            response.addHeader(ACA_HEADERS, X_REQ_WITH);
            response.addHeader(ACA_ORIGIN, "*");

            if (claimTO != null) {
                result = getMapper().writeValueAsString(claimTO);
            }
            return result;
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getobjectbypoint|getObjectByPoint}")
    public String getObjectByPoint(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @QueryParam(value = "x") String x,
            @QueryParam(value = "y") String y,
            @QueryParam(value = "projectId") String projectId,
            @Context HttpServletResponse response) {
        try {
            if (StringUtility.isEmpty(x) || StringUtility.isEmpty(y)) {
                throw ExceptionFactory.buildGeneralException(ServiceMessage.OT_WS_EMPTY_REQUIRED_FIELDS, localeCode);
            }

            // First look for claims as most top object
            ClaimSearchResultTO claimTO = CsGenericTranslator
                    .toTO(searchEjb.getClaimByCoordinates(x, y, projectId, localeCode), ClaimSearchResultTO.class);
            String result = "";
            response.addHeader(ACA_HEADERS, X_REQ_WITH);
            response.addHeader(ACA_ORIGIN, "*");

            if (claimTO != null) {
                result = getMapper().writeValueAsString(claimTO);
            } else {
                // Look for boundary
                AdministrativeBoundaryWithGeomSearchResultTO boundaryTO = CsGenericTranslator
                        .toTO(searchEjb.getAdministrativeBoundaryByCoordinates(x, y, projectId, localeCode), AdministrativeBoundaryWithGeomSearchResultTO.class);
                if (boundaryTO != null) {
                    result = getMapper().writeValueAsString(boundaryTO);
                }
            }
            return result;
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:searchmap|searchMap}")
    public String searchMap(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @QueryParam(value = "query") String query,
            @QueryParam(value = "projectId") String projectId,
            @Context HttpServletResponse response) {
        try {
            if (StringUtility.isEmpty(query)) {
                return null;
            }

            List<MapSearchResultTO> searchresults = CsGenericTranslator.toTOList(searchEjb.searchMap(query, projectId), MapSearchResultTO.class);
            String result = "";
            response.addHeader(ACA_HEADERS, X_REQ_WITH);
            response.addHeader(ACA_ORIGIN, "*");

            if (searchresults != null) {
                result = getMapper().writeValueAsString(searchresults);
            }
            return result;
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getallclaims|getAllClaims}/{projectId}")
    public String getAllClaims(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @PathParam(value = "projectId") String projectId) {
        try {
            List<ClaimSpatialSearchResultTO> claimsTOList = CsGenericTranslator
                    .toTOList(searchEjb.getAllClaims(projectId), ClaimSpatialSearchResultTO.class);
            if (claimsTOList == null) {
                return getMapper().writeValueAsString(new ArrayList<>());
            }
            return getMapper().writeValueAsString(claimsTOList);
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getjsonclaims|getJsonClaims}")
    public String getJsonClaims(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @QueryParam(value = "boundaryid") String boundaryId,
            @QueryParam(value = "projectId") String projectId,
            @Context HttpServletResponse response) {
        try {
            List<GeoJsonClaim> claims = searchEjb.getGeoJsonClaimsByBoundary(boundaryId, projectId);

            String feature = "\n{"
                    + "\"type\": \"Feature\","
                    + "\"properties\": {\"id\": \"%s\", \"nr\": \"%s\"},"
                    + "\"geometry\": %s"
                    + "}";
            String features = "";

            if (claims != null && !claims.isEmpty()) {
                for (GeoJsonClaim claim : claims) {
                    if (!StringUtility.isEmpty(claim.getGeom())) {
                        if (!StringUtility.isEmpty(features)) {
                            features += ", ";
                        }
                        features += String.format(feature, claim.getId(), claim.getNr(), claim.getGeom());
                    }
                }
            }

            response.addHeader(ACA_HEADERS, X_REQ_WITH);
            response.addHeader(ACA_ORIGIN, "*");

            return String.format(geoJson, features);
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getjsonboundary|getJsonBoundary}")
    public String getJsonBoundary(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @QueryParam(value = "id") String id,
            @QueryParam(value = "projectId") String projectId,
            @Context HttpServletResponse response) {
        try {
            GeoJsonAdministrativeBoundary boundary = searchEjb.getGeoJsonAdministrativeBoundary(id, projectId);

            String feature = "\n{"
                    + "\"type\": \"Feature\","
                    + "\"properties\": {\"id\": \"%s\", \"name\": \"%s\"},"
                    + "\"geometry\": %s"
                    + "}";
            String features = "";

            if (boundary != null && !StringUtility.isEmpty(boundary.getGeom())) {
                features = String.format(feature, boundary.getId(), boundary.getName(), boundary.getGeom());
            }

            response.addHeader(ACA_HEADERS, X_REQ_WITH);
            response.addHeader(ACA_ORIGIN, "*");

            return String.format(geoJson, features);
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @POST
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:uploadchunk|uploadChunk}/{projectId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String uploadChunk(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @PathParam(value = "projectId") String projectId,
            @FormDataParam("chunk") InputStream chunkBinary,
            @FormDataParam("descriptor") String chunkDescriptor) {
        try {

            AttachmentChunkTO chunkTO;
            try {
                chunkTO = getMapper().readValue(chunkDescriptor, AttachmentChunkTO.class);
            } catch (Exception e) {
                throw ExceptionFactory.buildBadJson(localeCode);
            }

            AttachmentChunk chunk = CsGenericTranslator.fromTO(chunkTO, AttachmentChunk.class, null);
            chunk.setBody(IOUtils.getInputStreamBytes(chunkBinary));

            claimEjb.saveAttachmentChunk(chunk, projectId);
            return ResponseFactory.buildOk();
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @POST
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:saveattachment|saveAttachment}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String saveAttachment(
            @PathParam(value = LOCALE_CODE) String localeCode,
            String attachmentDescriptor) {
        try {

            AttachmentTO attachmentTO;
            try {
                attachmentTO = getMapper().readValue(attachmentDescriptor, AttachmentTO.class);
            } catch (Exception e) {
                throw ExceptionFactory.buildBadJson(localeCode);
            }

            claimEjb.saveAttachmentFromChunks(CsGenericTranslator
                    .fromTO(attachmentTO, AttachmentBinary.class, null));
            return ResponseFactory.buildOk();
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @POST
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:saveclaim|saveClaim}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String saveClaim(
            @PathParam(value = LOCALE_CODE) final String localeCode,
            String claimDescriptor) {
        try {

            ClaimTO claimTO;
            try {
                claimTO = getMapper().readValue(claimDescriptor, ClaimTO.class);
            } catch (Exception e) {
                //throw ExceptionFactory.buildBadJson(localeCode);
                LogUtility.log("Failed to convert claim JSON", e);
                throw new OTRestException(400, e.getLocalizedMessage());
            }

            Claim existingClaim = claimEjb.getClaim(claimTO.getId(), true);
            Claim claim = CsGenericTranslator.fromTO(claimTO, Claim.class, existingClaim);
            if(!systemEjb.canAccessProject(claim.getProjectId())) {
                throw new OTProjectNotAccessible("");
            }
            
            final Claim[] claims = new Claim[]{claim};

            runUpdate(new Runnable() {
                @Override
                public void run() {
                    claims[0] = claimEjb.saveClaim(claims[0], localeCode);
                    claimEjb.submitClaim(claims[0].getId(), localeCode);
                }
            });

            return ResponseFactory.buildClaimSubmission(claimEjb.getClaim(claim.getId(), false));
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    @POST
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:saveboundary|saveBoundary}")
    @Consumes(MediaType.APPLICATION_JSON)
    public String saveBoundary(
            @PathParam(value = LOCALE_CODE) final String localeCode,
            String boundaryDescriptor) {
        try {

            AdministrativeBoundaryTO boundaryTO;
            try {
                boundaryTO = getMapper().readValue(boundaryDescriptor, AdministrativeBoundaryTO.class);
            } catch (Exception e) {
                //throw ExceptionFactory.buildBadJson(localeCode);
                LogUtility.log("Failed to convert boundary JSON", e);
                throw new OTRestException(400, e.getLocalizedMessage());
            }

            AdministrativeBoundary existingBoundary = claimEjb.getAdministrativeBoundary(boundaryTO.getId());
            if(existingBoundary != null){
                throw ExceptionFactory.buildObjectExist(ServiceMessage.OT_WS_BOUNDARY_EXISTS, localeCode);
            }
            
            AdministrativeBoundary boundary = CsGenericTranslator.fromTO(boundaryTO, AdministrativeBoundary.class, existingBoundary);
            if(!systemEjb.canAccessProject(boundary.getProjectId())) {
                throw new OTProjectNotAccessible("");
            }
            final AdministrativeBoundary[] boundarys = new AdministrativeBoundary[]{boundary};

            runUpdate(new Runnable() {
                @Override
                public void run() {
                    boundarys[0] = claimEjb.saveAdministrativeBoundary(boundarys[0]);
                }
            });

            return ResponseFactory.buildResultResponse(boundarys[0].getId());
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getattachmentlastchunk|getAttachmentLastChunk}/{attachmentId}")
    public String getAttachmentLastChunk(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @PathParam(value = "attachmentId") String attachmentId) {
        try {
            AttachmentChunkTO chunkTO = CsGenericTranslator
                    .toTO(claimEjb.getAttachmentLastChunk(attachmentId), AttachmentChunkTO.class);
            if (chunkTO == null) {
                throw ExceptionFactory.buildNotFound(localeCode);
            }
            return getMapper().writeValueAsString(chunkTO);
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getattachmentchunks|getAttachmentChunks}/{attachmentId}")
    public String getAttachmentChunks(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @PathParam(value = "attachmentId") String attachmentId) {
        try {
            List<AttachmentChunkTO> attachmentChunkTOList = CsGenericTranslator
                    .toTOList(claimEjb.getAttachmentChunks(attachmentId), AttachmentChunkTO.class);
            if (attachmentChunkTOList == null) {
                throw ExceptionFactory.buildNotFound(localeCode);
            }
            return getMapper().writeValueAsString(attachmentChunkTOList);
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:deleteattachmentchunks|deleteAttachmentChunks}/{attachmentId}")
    public String deleteAttachmentChunks(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @PathParam(value = "attachmentId") String attachmentId) {
        try {
            claimEjb.deleteAttachmentChunks(attachmentId);
            return ResponseFactory.buildOk();
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:deleteclaimchunks|deleteClaimChunks}/{claimId}")
    public String deleteClaimChunks(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @PathParam(value = "claimId") String claimId) {
        try {
            claimEjb.deleteClaimChunks(claimId);
            return ResponseFactory.buildOk();
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path(value = "{a:getattachmentfile|getAttachmentFile}/{attachmentId}")
    public Response getAttachmentFile(
            @Context HttpServletResponse response,
            @PathParam(value = LOCALE_CODE) String localeCode,
            @PathParam(value = "attachmentId") String attachmentId) {
        try {
            BufferedOutputStream output = null;
            ByteArrayInputStream input = null;

            try {
                AttachmentBinary attachment = claimEjb.getAttachment(attachmentId);
                if (attachment == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }

                input = new ByteArrayInputStream(attachment.getBody());
                int contentLength = input.available();
                response.setContentLength(contentLength);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + attachment.getFileName() + "\"");
                response.setContentType(attachment.getMimeType());

                output = new BufferedOutputStream(response.getOutputStream());
                int data = input.read();
                while (data != -1) {
                    output.write(data);
                    data = input.read();
                }
                output.flush();
            } catch (IOException e) {
                throw ExceptionFactory.buildNotFound(localeCode);
            } finally {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            }
            return Response.ok().build();

        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:deleteclaim|deleteClaim}/{claimId}")
    public String deleteClaim(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @PathParam(value = "claimId") String claimId) {
        try {

            try {
                claimEjb.deleteClaim(claimId);
            } catch (Exception e) {
                throw processException(e, localeCode);
            }
            return ResponseFactory.buildOk();
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:withdrawclaim|withdrawClaim}/{claimId}")
    public String withdrawClaim(
            @PathParam(value = LOCALE_CODE) String localeCode,
            @PathParam(value = "claimId") String claimId) {
        try {

            try {
                claimEjb.withdrawClaim(claimId);
            } catch (Exception e) {
                throw processException(e, localeCode);
            }
            return ResponseFactory.buildOk();
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getparcelgeomrequired|getParcelGeomRequired}/{projectId}")
    public String getParcelGeomRequired(@PathParam(value = LOCALE_CODE) String localeCode, @PathParam(value = "projectId") String projectId) {
        try {
            String result = systemEjb.getSetting(ConfigConstants.REQUIRES_SPATIAL, projectId, "1");
            return "{result:\"" + result + "\"}";
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }

    @GET
    @Produces("application/json; charset=UTF-8")
    @Path(value = "{a:getapprovedadministrativeboundaries|getApprovedAdministrativeBoundaries}")
    /**
     * Returns approved administrative boundaries
     */
    public String getApprovedAdministrativeBoundaries(@PathParam(value = LOCALE_CODE) String localeCode) {
        try {
            List<AdministrativeBoundaryTO> boundaries = CsGenericTranslator.toTOList(claimEjb.getApprovedAdministrativeBoundaries(), AdministrativeBoundaryTO.class);

            if (boundaries == null) {
                return getMapper().writeValueAsString(new ArrayList<>());
                //throw ExceptionFactory.buildNotFound(localeCode);
            }
            return getMapper().writeValueAsString(boundaries);
        } catch (Exception e) {
            throw processException(e, localeCode);
        }
    }
    
    private void removeClaimPrivateInfo(ClaimTO claim) {
        if (claim == null) {
            return;
        }

        if (claimEjb.isInRole(RolesConstants.CS_MODERATE_CLAIM, RolesConstants.CS_REVIEW_CLAIM)) {
            return;
        }

        if (StringUtility.empty(claim.getRecorderName()).equalsIgnoreCase(claimEjb.getUserName())) {
            return;
        }

        removePartyPrivateInfo(claim.getClaimant());

        if (claim.getShares() != null) {
            for (ClaimShareTO share : claim.getShares()) {
                if (share.getOwners() != null) {
                    for (ClaimPartyTO owner : share.getOwners()) {
                        removePartyPrivateInfo(owner);
                    }
                }
            }
        }
    }

    private void removePartyPrivateInfo(ClaimPartyTO party) {
        if (party != null) {
            if (StringUtility.isEmpty(hiddenString)) {
                hiddenString = MessageUtility.getLocalizedMessageText(ServiceMessage.OT_WS_HIDDEN);
            }
            party.setPhone(hiddenString);
            party.setMobilePhone(hiddenString);
            party.setEmail(hiddenString);
        }
    }
}
