/*-
 * #%L
 * an ImageJ plugin for analysis of acceptor photobleaching FRET images.
 * %%
 * Copyright (C) 2008 - 2020 AccPbFRET developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package hu.unideb.med.biophys;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 *
 */
class HelpWindow extends JFrame {

    private AccPbFRET_Plugin accBlWindow;
    private JPanel panel;

    public HelpWindow(AccPbFRET_Plugin accBlWindow) {
        setTitle("AccPbFRET Help");
        this.accBlWindow = accBlWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(600, 800);
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
    }

    public void createGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);
        setFont(new Font("Helvetica", Font.PLAIN, 12));

        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 2;
        JLabel label1 = new JLabel("<html><center><b>AccPbFRET Help</b></center></html>");
        label1.setFont(new Font("Helvetica", Font.BOLD, 14));
        panel.add(label1, gc);

        gc.gridy = GridBagConstraints.RELATIVE;

        JLabel label1b = new JLabel("<html><b><br><u>Menu structure</u></b></html>");
        panel.add(label1b, gc);
        JLabel label1c = new JLabel("<html>The \"File\" and \"Image\" menus include ImageJ commands that are likely to be frequently used in<br>the analysis process.<br><br>Also, messages generated during image processing can be saved or cleared from the \"File\" menu.<br>Furthermore, analysis data can be reset from the \"File\" menu and the plugin can be switched to<br>semi-automatic mode here as well.<br><br>In the \"Corrections\" menu, the checkboxes of desired correction algorithms need to be checked.<br><br>Donor bleaching is almost inevitable during the acquisition of sequential donor images and using<br>this factor (or at least checking its value) is highly recommended. When this correction is chosen,<br>the default value of 1% unwanted donor bleaching is assigned (yielding a factor of 1.01).<br><br>Acceptor crosstalk can be a problem depending on the filter sets and laser of choice. When this<br>correction is chosen, the default value of 1% unwanted acceptor bleed-through to the donor<br>channel is assigned (yielding a factor of 0.01).<br><br>Acceptor photoproduct(s) can be fluorescent and cross-talk into the donor channel. When<br>correction for this possibility is chosen, the default value of 1% unwanted acceptor photoprodocut<br>cross-talk to the donor channel  is assigned (yielding a factor of 0.01).<br><br>Partial acceptor photobleaching causes the underestimation of FRET. The correction for this<br>assumes a linear relation between the number of available (unbleached) acceptor molecules<br>and the efficiency of FRET. The default value 0 for this correction factor means a total (100%)<br>photodestruction of the acceptor.<br></html>");
        panel.add(label1c, gc);

        JLabel label2 = new JLabel("<html><b><br><u>Step 1: Opening and setting images</u></b></html>");
        panel.add(label2, gc);
        JLabel label3 = new JLabel("<html>Image files can be opened with the \"Open\" button, or with the \"Open image\" item in the \"File\"<br>menu. After opening, images can be set as donor before or after, or acceptor before or after<br>using the \"Set image\" buttons. If multichannel images are used (either having before and after<br>images, or donor and acceptor channel images, or both) in consecutive layers of stacked image<br>files, the opened image has to be split (item available from the \"Image\" menu) before setting. If<br>the \"Use LSM\" checkbox is checked, the LSM image files (up to AIM v. 4.x, files with *.lsm<br>extension) containing donor and acceptor channels in a time series encompassing both before<br>and after photobleaching images are split and set automatically after opening with the \"Open &<br>Set LSM\" button. Every previously opened image window will be closed after pressing this button.<br></html>");
        panel.add(label3, gc);

        JLabel label4 = new JLabel("<html><b><br><u>Step 2: Registration of donor images</u></b></html>");
        panel.add(label4, gc);
        JLabel label5 = new JLabel("<html>To register the donor images, press the \"Register\" button. If the checkbox is checked, acceptor<br>image pairs will automatically be resgistered using the same shift. This is important for<br>deterimining the correction factor for incomplete bleaching of the acceptor.</html>");
        panel.add(label5, gc);

        JLabel label6 = new JLabel("<html><b><br><u>Step 3: Subtraction of background of images</u></b></html>");
        panel.add(label6, gc);
        JLabel label7 = new JLabel("<html>To subtract background (the average of pixels in a selected ROI), the \"Subtract\" button has to<br>be pressed for each relevant image. The \"Copy\" button copies the ROI of the first image to the<br>others. This should be done after marking the ROI and before applying the subtraction. To avoid<br>incidental reusing of the ROI in further operations (such as Gaussian blurring), the ROI is<br>automatically deleted after applying the background correction.</html>");
        panel.add(label7, gc);

        JLabel label8 = new JLabel("<html><b><br><u>Step 4: Gaussian blurring of images</u></b></html>");
        panel.add(label8, gc);
        JLabel label9 = new JLabel("<html>Images can be blurred with the given radius by pressing the corresponding \"Blur\" button.<br>Blurring (together with thresholding) can be reverted using the \"Reset\" buttons in Step 5.</html>");
        panel.add(label9, gc);

        JLabel label10 = new JLabel("<html><b><br><u>Step 5: Setting thresholds for the images</u></b></html>");
        panel.add(label10, gc);
        JLabel label11 = new JLabel("<html>Thresholds can be applied to the images by pressing the corresponding \"Threshold\" button.<br>After setting the threshold, the \"Apply\" button has to be pressed on bottom menu of the<br>\"Threshold\" window. After this, select \"Set background pixels to NaN\" and press \"Ok\".<br>Closing \"Threshold\" window will apply the thresholding LUT to the active image and therefore<br>should be avoided.<br>The \"Reset\" buttons reset both blur and threshold settings of the corresponding image.</html>");
        panel.add(label11, gc);

        JLabel label12 = new JLabel("<html><b><br><u>Correction 1: Calculation and setting of donor bleaching correction factor</u></b></html>");
        panel.add(label12, gc);
        JLabel label13 = new JLabel("<html>After pressing the \"Calculate\" button, a new window pops up, where the calculation of this factor<br>can be done with setting donor before and after photobleaching images of a sample labeled with<br>donor only, and taking similar steps as in the main window of<br>the program.</html>");
        panel.add(label13, gc);

        JLabel label14 = new JLabel("<html><b><br><u>Correction 2: Calculation and setting of acceptor cross-talk correction factor</u></b></html>");
        panel.add(label14, gc);
        JLabel label15 = new JLabel("<html>After pressing the \"Calculate\" button, a new window pops up, where the calculation of this factor<br>can be done with setting donor and acceptor images of a sample labeled with acceptor only<br>(before photobleaching), and taking similar steps as in the main window of the program.</html>");
        panel.add(label15, gc);

        JLabel label16 = new JLabel("<html><b><br><u>Correction 3: Calculation and setting of acceptor photoproduct correction factor</u></b></html>");
        panel.add(label16, gc);
        JLabel label17 = new JLabel("<html>After pressing the \"Calculate\" button, a new window pops up, where the calculation of this factor<br>can be done with setting donor (after photobleaching) and acceptor (before photobleaching)<br>images of a sample labeled with acceptor only, and taking similar steps as in the main window of<br>the program.</html>");
        panel.add(label17, gc);

        JLabel label18 = new JLabel("<html><b><br><u>Correction 4: Calculation of partial acceptor photobleaching correction factor</u></b></html>");
        panel.add(label18, gc);
        JLabel label19 = new JLabel("<html>To calculate this factor, press the \"Calculate\" button. The acceptor before and after photobleaching<br>images have to be set before the calculation. Usually this will already have been done in step 1,<br>where the measurement files (or stacked file) is opened.</html>");
        panel.add(label19, gc);

        JLabel label20 = new JLabel("<html><b><br><u>Step 6: Creation of the transfer (FRET efficiency) image</u></b></html>");
        panel.add(label20, gc);
        JLabel label21 = new JLabel("<html>After pressing the \"Create\" button, the transfer image will be calculated and displayed. If the<br>\"Results\" window is not open it will be opened too. If the \"Use also acceptor before image as<br>mask\" option is checked, the FRET image will be created by AND-ing this mask to the threshold<br>masks of the donor before and after images. At this point, the acceptor before image can be<br>cleared (at step 1), and any other relevant image can be loaded and applied as an additional<br>mask after processing and thresholding this image as desired (going through steps 3c, 4c and<br>5c).</html>");
        panel.add(label21, gc);

        JLabel label22 = new JLabel("<html><b><br><u>Step 7: Making measurements</u></b></html>");
        panel.add(label22, gc);
        JLabel label23 = new JLabel("<html>After the creation of the transfer image, ROIs can be selected on it, and measurements can be<br>made by pressing the \"Measure\" button. FRET histograms can be most easily viewed and<br>exported by clicking the \"Histogram\" item in the \"Image\" menu.</html>");
        panel.add(label23, gc);

        JLabel label24 = new JLabel("<html><br></html>");
        panel.add(label24, gc);

        JScrollPane logScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        getContentPane().add(logScrollPane);
    }
}
