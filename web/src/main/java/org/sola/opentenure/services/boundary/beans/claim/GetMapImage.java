package org.sola.opentenure.services.boundary.beans.claim;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import jakarta.ejb.EJB;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.sola.common.StringUtility;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import javax.imageio.ImageIO;
import org.sola.common.ConfigConstants;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.sola.cs.services.ejbs.claim.businesslogic.MapImageEJBLocal;
import org.sola.cs.services.ejbs.claim.entities.MapImageParams;
import org.sola.cs.services.ejbs.claim.entities.MapImageResponse;
import org.sola.opentenure.services.boundary.beans.project.ProjectBean;
import org.sola.services.common.logging.LogUtility;

/**
 * Returns map image with claims
 */
@Named
@ViewScoped
public class GetMapImage extends AbstractBackingBean {

    @EJB
    MapImageEJBLocal mapEjb;
    
    @EJB
    SystemCSEJBLocal systemEjb;
    
    @Inject
    ProjectBean projectBean;
    
    public BufferedImage getMapImage() throws IOException {
        return getMapImage(getRequestParam("id"));
    }

    public BufferedImage getMapImage(String claimId, MapImageParams mapParams) throws IOException {
        try {
            mapParams.setUseGoogleBackground(systemEjb.getSetting(ConfigConstants.OT_USE_BACKGROUND_GOOGLE_MAP, projectBean.getProjectId(), "0").equals("1"));
            mapParams.setUseWmsBackground(systemEjb.getSetting(ConfigConstants.OT_USE_BACKGROUND_WMS_MAP, projectBean.getProjectId(), "0").equals("1"));
            
            MapImageResponse mapReponse = mapEjb.getMapImage(claimId, projectBean.getProjectId(), mapParams);

            if (mapReponse != null && mapReponse.getMap() != null) {
                return mapReponse.getMap();
            }
        } catch (Exception e) {
            LogUtility.log(e.getMessage());
        }
        return drawEmptyImage(mapParams.getWidth(), mapParams.getHeight());
    }

    public BufferedImage getMapImage(String claimId) throws IOException {
        int width = 700;
        int height = 400;
        String scaleLabel = "Scale";
        boolean drawScale = true;

        try {
            if (StringUtility.isEmpty(claimId)) {
                return drawEmptyImage(width, height);
            }
            if (!StringUtility.isEmpty(getRequestParam("width"))) {
                width = Integer.parseInt(getRequestParam("width"));
            }
            if (!StringUtility.isEmpty(getRequestParam("height"))) {
                height = Integer.parseInt(getRequestParam("height"));
            }
            if (!StringUtility.isEmpty(getRequestParam("drawscale"))) {
                drawScale = Boolean.parseBoolean(getRequestParam("drawscale"));
            }
            if (!StringUtility.isEmpty(getRequestParam("scalelabel"))) {
                scaleLabel = getRequestParam("scalelabel");
            }

            MapImageParams mapParams = new MapImageParams();
            mapParams.setForPdf(true);
            mapParams.setHeight(height);
            mapParams.setWidth(width);
            mapParams.setShowScale(drawScale);
            mapParams.setScaleLabel(scaleLabel);
            
            return getMapImage(claimId, mapParams);
        } catch (Exception e) {
            LogUtility.log(e.getMessage());
        }
        return drawEmptyImage(width, height);
    }

    public void getMapImageWeb() throws IOException {
        BufferedImage mapImage;

        int width = 700;
        int height = 400;
        String scaleLabel = "Scale";
        boolean drawScale = true;
        
        if (!StringUtility.isEmpty(getRequestParam("width"))) {
            width = Integer.parseInt(getRequestParam("width"));
        }
        if (!StringUtility.isEmpty(getRequestParam("height"))) {
            height = Integer.parseInt(getRequestParam("height"));
        }
        if (!StringUtility.isEmpty(getRequestParam("drawscale"))) {
            drawScale = Boolean.parseBoolean(getRequestParam("drawscale"));
        }
        if (!StringUtility.isEmpty(getRequestParam("scalelabel"))) {
            scaleLabel = getRequestParam("scalelabel");
        }

        MapImageParams mapParams = new MapImageParams();
        mapParams.setForPdf(false);
        mapParams.setHeight(height);
        mapParams.setWidth(width);
        mapParams.setShowScale(drawScale);
        mapParams.setScaleLabel(scaleLabel);

        mapImage = getMapImage(getRequestParam("id"), mapParams);

        FacesContext fc = getContext();
        ExternalContext ec = fc.getExternalContext();
        ec.responseReset();

        ec.setResponseContentType("image/jpeg");
        ec.setResponseCharacterEncoding("utf-8");
        ec.setResponseHeader("Content-Disposition", "inline; filename=\"map.jpeg\"");

        OutputStream output = ec.getResponseOutputStream();
        ImageIO.write(mapImage, "jpeg", output);
        fc.responseComplete();
    }

    private BufferedImage drawEmptyImage(int width, int height) {
        BufferedImage mapImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D grMapImage = mapImage.createGraphics();
        grMapImage.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        grMapImage.setPaint(Color.WHITE);
        grMapImage.fill(new Rectangle(width, height));
        grMapImage.setPaint(Color.RED);

        String txt = "FAILED TO GENERATE IMAGE";
        FontMetrics fontMetrics = grMapImage.getFontMetrics();
        int txtWidth = fontMetrics.stringWidth(txt);
        grMapImage.drawString(txt, (width / 2) - (txtWidth / 2), (height / 2) + 5);
        return mapImage;
    }
}
