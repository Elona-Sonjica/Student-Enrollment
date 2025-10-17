/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.util;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
/**
 *
 * @author elzas
 */

public class RoundedImageUtil {
    
    public static ImageIcon createRoundedImageIcon(String imagePath, int size) {
        try {
            // Load the image from the classpath
            Image img = new ImageIcon(RoundedImageUtil.class.getResource(imagePath)).getImage();
            if (img == null) {
                System.err.println("Image not found: " + imagePath);
                return createDefaultIcon(size);
            }
            
            BufferedImage rounded = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = rounded.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, size, size);
            g2.setClip(circle);

            g2.drawImage(img, 0, 0, size, size, null);
            g2.dispose();

            return new ImageIcon(rounded);
        } catch (Exception e) {
            System.err.println("Error creating rounded image: " + e.getMessage());
            return createDefaultIcon(size);
        }
    }
    
    private static ImageIcon createDefaultIcon(int size) {
        BufferedImage defaultImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = defaultImage.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillOval(0, 0, size, size);
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        
        String text = "CPUT";
        FontMetrics fm = g2.getFontMetrics();
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size + fm.getAscent()) / 2;
        g2.drawString(text, x, y);
        
        g2.dispose();
        return new ImageIcon(defaultImage);
    }
    
    public static ImageIcon createScaledImageIcon(String imagePath, int width, int height) {
        try {
            Image img = new ImageIcon(RoundedImageUtil.class.getResource(imagePath)).getImage();
            if (img == null) {
                System.err.println("Image not found: " + imagePath);
                return null;
            }
            Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (Exception e) {
            System.err.println("Error creating scaled image: " + e.getMessage());
            return null;
        }
    }
}