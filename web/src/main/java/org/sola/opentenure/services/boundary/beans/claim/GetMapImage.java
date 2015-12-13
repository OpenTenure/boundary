package org.sola.opentenure.services.boundary.beans.claim;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import javax.ejb.EJB;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.sola.common.StringUtility;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import javax.imageio.ImageIO;
import org.sola.cs.services.ejbs.claim.businesslogic.MapImageEJBLocal;

/**
 * Returns map image with claims
 */
@Named
@ViewScoped
public class GetMapImage extends AbstractBackingBean {

    @EJB
    MapImageEJBLocal mapEjb;

    public void getMapImage() throws IOException {
        BufferedImage mapImage = null;
        int width = 700;
        int height = 400;
        String scaleLabel = "Scale";
        boolean drawScale = true;
        
        try {
            String id = getRequestParam("id");
            
            if (StringUtility.isEmpty(id)) {
                return;
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
            
            mapImage = mapEjb.getMapImage(id, width, height,drawScale, scaleLabel);

            if (mapImage == null) {
                mapImage = drawEmptyImage(width, height);
            }
        } catch (Exception e) {
            mapImage = drawEmptyImage(width, height);
        }

        FacesContext fc = getContext();
        ExternalContext ec = fc.getExternalContext();
        ec.responseReset();
        
        ec.setResponseContentType("image/jpeg");
        ec.setResponseCharacterEncoding("utf-8");
        //ec.setResponseContentLength(mapImage);
        
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
