/* 
 * Copyright (C) 2008 - 2019 AccPbFRET developers.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hu.unideb.med.biophys;

import ij.ImagePlus;
import ij.WindowManager;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 *
 */
public class ApplyMaskDialog extends JDialog implements ActionListener {

    private AccPbFRET_Plugin accBlWindow;
    private ImagePlus toMaskImg;
    private ImagePlus maskImg;
    private JPanel panel;
    private JButton setToMaskImgButton;
    private JButton setMaskImgButton;
    private JButton createImagesButton;

    public ApplyMaskDialog(AccPbFRET_Plugin accBlWindow) {
        setTitle("Apply Mask to An Image");
        this.accBlWindow = accBlWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(275, 240);
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2, 2, 6, 2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = 2;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>After setting an image to mask and a mask image (with NaN background pixels), two images will be created. The first one will contain the pixels which are not NaN in the mask, and the second one the others.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridx = 0;
        gc.gridy = 1;
        setToMaskImgButton = new JButton("Set image to be masked");
        setToMaskImgButton.addActionListener(this);
        setToMaskImgButton.setActionCommand("setImageToMask");
        panel.add(setToMaskImgButton, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        setMaskImgButton = new JButton("Set mask image (with NaN bg. pixels)");
        setMaskImgButton.addActionListener(this);
        setMaskImgButton.setActionCommand("setMaskImage");
        panel.add(setMaskImgButton, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        createImagesButton = new JButton("Create masked images");
        createImagesButton.addActionListener(this);
        createImagesButton.setActionCommand("createImages");
        panel.add(createImagesButton, gc);

        getContentPane().add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (e.getActionCommand()) {
                case "setImageToMask":
                    toMaskImg = WindowManager.getCurrentImage();
                    if (toMaskImg == null) {
                        accBlWindow.logError("No image is selected. (Masking)");
                        return;
                    }
                    if (toMaskImg.getImageStackSize() > 1) {
                        accBlWindow.logError("Current image contains more than 1 channel (" + toMaskImg.getImageStackSize() + "). Please split it into parts. (Masking)");
                        toMaskImg = null;
                        return;
                    } else if (toMaskImg.getNSlices() > 1) {
                        accBlWindow.logError("Current image contains more than 1 slice (" + toMaskImg.getNSlices() + "). Please split it into parts. (Masking)");
                        toMaskImg = null;
                        return;
                    }
                    toMaskImg.setTitle("Image to mask - " + new Date().toString());
                    new ImageConverter(toMaskImg).convertToGray32();
                    setToMaskImgButton.setBackground(accBlWindow.greenColor);
                    setToMaskImgButton.setOpaque(true);
                    setToMaskImgButton.setBorderPainted(false);
                    break;
                case "setMaskImage":
                    maskImg = WindowManager.getCurrentImage();
                    if (maskImg == null) {
                        accBlWindow.logError("No image is selected. (Masking)");
                        return;
                    }
                    if (maskImg.getImageStackSize() > 1) {
                        accBlWindow.logError("Current image contains more than 1 channel (" + maskImg.getImageStackSize() + "). Please split it into parts. (Masking)");
                        maskImg = null;
                        return;
                    } else if (maskImg.getNSlices() > 1) {
                        accBlWindow.logError("Current image contains more than 1 slice (" + maskImg.getNSlices() + "). Please split it into parts. (Masking)");
                        maskImg = null;
                        return;
                    }
                    maskImg.setTitle("Mask image - " + new Date().toString());
                    new ImageConverter(maskImg).convertToGray32();
                    setMaskImgButton.setBackground(accBlWindow.greenColor);
                    setMaskImgButton.setOpaque(true);
                    setMaskImgButton.setBorderPainted(false);
                    break;
                case "createImages":
                    if (toMaskImg == null) {
                        accBlWindow.logError("No image to mask is set. (Masking)");
                        return;
                    } else if (maskImg == null) {
                        accBlWindow.logError("No mask image is set. (Masking)");
                        return;
                    }
                    ImageProcessor ipTM = toMaskImg.getProcessor();
                    ImageProcessor ipM = maskImg.getProcessor();
                    float[] ipTMP = (float[]) ipTM.getPixels();
                    float[] ipMP = (float[]) ipM.getPixels();
                    int width = ipTM.getWidth();
                    int height = ipTM.getHeight();
                    float[][] img1Points = new float[width][height];
                    float[][] img2Points = new float[width][height];
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (!Float.isNaN(ipMP[width * j + i])) {
                                img1Points[i][j] = ipTMP[width * j + i];
                                img2Points[i][j] = Float.NaN;
                            } else {
                                img1Points[i][j] = Float.NaN;
                                img2Points[i][j] = ipTMP[width * j + i];
                            }
                        }
                    }
                    FloatProcessor fp1 = new FloatProcessor(img1Points);
                    FloatProcessor fp2 = new FloatProcessor(img2Points);
                    ImagePlus img2 = new ImagePlus("Masked image 2 (pixels outside the mask)", fp2);
                    img2.show();
                    ImagePlus img1 = new ImagePlus("Masked image 1 (pixels in the mask)", fp1);
                    img1.show();
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            accBlWindow.logException(t.toString(), t);
        }
    }
}
