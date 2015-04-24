package org.sola.opentenure.services.boundary.beans.helpers;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.awt.font.TextAttribute;
 
public class CaptchaImage extends HttpServlet {
 
    private int height = 0;
    private int width = 0;
    public static final String CAPTCHA_KEY = "captcha_key_name";
 
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        height = Integer.parseInt(getServletConfig().getInitParameter("height"));
        width = Integer.parseInt(getServletConfig().getInitParameter("width"));
    }
 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {
        //Expire response
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Max-Age", 0);
 
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setColor(new Color(135, 169, 104));
        graphics2D.fillRect(0, 0, width, height);
        Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
        Random r = new Random();
        String token = Long.toString(Math.abs(r.nextLong()), 36);
        String ch = token.substring(0, 6);
        Color c = new Color(150,150,150);
        GradientPaint gp = new GradientPaint(30, 30, c, 15, 25, Color.white, true);
        graphics2D.setPaint(gp);
        Font font = new Font("Verdana", Font.CENTER_BASELINE, 26);
        graphics2D.setFont(font);
        graphics2D.drawString(ch, 7, 25);
        graphics2D.dispose();
 
        HttpSession session = req.getSession(true);
        session.setAttribute(CAPTCHA_KEY, ch);
 
        OutputStream outputStream = response.getOutputStream();
        ImageIO.write(image, "jpeg", outputStream);
        outputStream.close();
    }
}

