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

import ij.IJ;
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
import java.text.DecimalFormat;
import java.util.Date;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 *
 */
public class AcceptorCTCorrDialog extends JDialog implements ActionListener {

    private AccPbFRET_Plugin accBlWindow;
    private ImagePlus donorCBefore;
    private ImagePlus acceptorCBefore;
    private JPanel panel;
    private JButton setDonorBeforeButton;
    private JButton setAcceptorBeforeButton;
    private JButton setDonorBeforeThresholdButton;
    private JButton setAcceptorBeforeThresholdButton;
    private JButton calculateButton;
    private JButton setButton;
    private JButton subtractDonorBeforeButton;
    private JButton subtractAcceptorBeforeButton;
    private JButton resetButton;
    private ButtonGroup buttonGroup;
    private JRadioButton averagesButton;
    private JRadioButton quotientsButton;
    private JLabel mode1ResultLabel;
    private JLabel mode2ResultLabel;
    private JCheckBox showCTCImagesCB;

    public AcceptorCTCorrDialog(AccPbFRET_Plugin accBlWindow) {
        setTitle("Acceptor Cross-Talk Correction Factor");
        this.accBlWindow = accBlWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (IJ.isMacOSX()) {
            setSize(320, 385);
        } else {
            setSize(300, 385);
        }
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2, 2, 6, 2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>This factor is calculated based on images of donor and acceptor channels of an acceptor only labeled sample, before photobleaching.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridwidth = 3;
        gc.gridx = 0;
        gc.gridy = 1;
        setDonorBeforeButton = new JButton("Set donor before bleaching (acc. only)");
        setDonorBeforeButton.addActionListener(this);
        setDonorBeforeButton.setActionCommand("setDonorCBefore");
        panel.add(setDonorBeforeButton, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        setAcceptorBeforeButton = new JButton("Set acceptor before bleaching (acc. only)");
        setAcceptorBeforeButton.addActionListener(this);
        setAcceptorBeforeButton.setActionCommand("setAcceptorCBefore");
        panel.add(setAcceptorBeforeButton, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        subtractDonorBeforeButton = new JButton("Subtract background of donor before");
        subtractDonorBeforeButton.addActionListener(this);
        subtractDonorBeforeButton.setActionCommand("subtractDonorCBefore");
        panel.add(subtractDonorBeforeButton, gc);
        gc.gridx = 0;
        gc.gridy = 4;
        subtractAcceptorBeforeButton = new JButton("Subtract background of acceptor before");
        subtractAcceptorBeforeButton.addActionListener(this);
        subtractAcceptorBeforeButton.setActionCommand("subtractAcceptorCBefore");
        panel.add(subtractAcceptorBeforeButton, gc);
        gc.gridx = 0;
        gc.gridy = 5;
        setDonorBeforeThresholdButton = new JButton("Set donor before threshold");
        setDonorBeforeThresholdButton.addActionListener(this);
        setDonorBeforeThresholdButton.setActionCommand("setDonorCBeforeThreshold");
        panel.add(setDonorBeforeThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 6;
        setAcceptorBeforeThresholdButton = new JButton("Set acceptor before threshold");
        setAcceptorBeforeThresholdButton.addActionListener(this);
        setAcceptorBeforeThresholdButton.setActionCommand("setAcceptorCBeforeThreshold");
        panel.add(setAcceptorBeforeThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 7;
        gc.gridheight = 2;
        JPanel radioPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gcr = new GridBagConstraints();
        radioPanel.setLayout(gbl);
        gcr.insets = new Insets(0, 4, 4, 4);
        gcr.fill = GridBagConstraints.BOTH;
        JLabel modeLabel = new JLabel("Mode:");
        JLabel resultLabel = new JLabel("Result:");
        mode1ResultLabel = new JLabel("", JLabel.CENTER);
        mode2ResultLabel = new JLabel("", JLabel.CENTER);
        quotientsButton = new JRadioButton("Point-by-point");
        quotientsButton.setToolTipText("The factor is the averaged ratio of corresponding pixel values in the donor before and acceptor before photobleaching images.");
        averagesButton = new JRadioButton("Average pixels");
        averagesButton.setToolTipText("The factor is the ratio of the gated pixel averages in the donor before and acceptor before photobleaching images.");
        quotientsButton.setSelected(true);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(quotientsButton);
        buttonGroup.add(averagesButton);
        gcr.gridx = 0;
        gcr.gridy = 0;
        radioPanel.add(modeLabel, gcr);
        gcr.gridx = 1;
        gcr.gridy = 0;
        radioPanel.add(quotientsButton, gcr);
        gcr.gridx = 2;
        gcr.gridy = 0;
        radioPanel.add(averagesButton, gcr);
        gcr.gridx = 0;
        gcr.gridy = 1;
        radioPanel.add(resultLabel, gcr);
        gcr.gridx = 1;
        gcr.gridy = 1;
        radioPanel.add(mode1ResultLabel, gcr);
        gcr.gridx = 2;
        gcr.gridy = 1;
        radioPanel.add(mode2ResultLabel, gcr);
        panel.add(radioPanel, gc);
        gc.gridx = 0;
        gc.gridy = 9;
        gc.gridheight = 1;
        showCTCImagesCB = new JCheckBox("Show correction image (for manual calc.)");
        panel.add(showCTCImagesCB, gc);
        gc.gridwidth = 1;
        gc.gridheight = 1;
        gc.gridx = 0;
        gc.gridy = 10;
        calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(this);
        calculateButton.setActionCommand("calculate");
        panel.add(calculateButton, gc);
        gc.gridx = 1;
        gc.gridy = 10;
        setButton = new JButton("Set selected");
        setButton.addActionListener(this);
        setButton.setActionCommand("setfactor");
        panel.add(setButton, gc);
        gc.gridx = 2;
        gc.gridy = 10;
        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        resetButton.setActionCommand("reset");
        panel.add(resetButton, gc);

        getContentPane().add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (e.getActionCommand()) {
                case "reset":
                    donorCBefore = null;
                    acceptorCBefore = null;
                    setDonorBeforeButton.setBackground(accBlWindow.originalButtonColor);
                    setDonorBeforeButton.setOpaque(false);
                    setDonorBeforeButton.setBorderPainted(true);
                    setAcceptorBeforeButton.setBackground(accBlWindow.originalButtonColor);
                    setAcceptorBeforeButton.setOpaque(false);
                    setAcceptorBeforeButton.setBorderPainted(true);
                    setDonorBeforeThresholdButton.setBackground(accBlWindow.originalButtonColor);
                    setDonorBeforeThresholdButton.setOpaque(false);
                    setDonorBeforeThresholdButton.setBorderPainted(true);
                    setAcceptorBeforeThresholdButton.setBackground(accBlWindow.originalButtonColor);
                    setAcceptorBeforeThresholdButton.setOpaque(false);
                    setAcceptorBeforeThresholdButton.setBorderPainted(true);
                    calculateButton.setBackground(accBlWindow.originalButtonColor);
                    calculateButton.setOpaque(false);
                    calculateButton.setBorderPainted(true);
                    setButton.setBackground(accBlWindow.originalButtonColor);
                    setButton.setOpaque(false);
                    setButton.setBorderPainted(true);
                    subtractDonorBeforeButton.setBackground(accBlWindow.originalButtonColor);
                    subtractDonorBeforeButton.setOpaque(false);
                    subtractDonorBeforeButton.setBorderPainted(true);
                    subtractAcceptorBeforeButton.setBackground(accBlWindow.originalButtonColor);
                    subtractAcceptorBeforeButton.setOpaque(false);
                    subtractAcceptorBeforeButton.setBorderPainted(true);
                    mode1ResultLabel.setText("");
                    mode2ResultLabel.setText("");
                    break;
                case "setDonorCBefore":
                    donorCBefore = WindowManager.getCurrentImage();
                    if (donorCBefore == null) {
                        accBlWindow.logError("No image is selected. (ct. corr.)");
                        return;
                    }
                    if (donorCBefore.getNChannels() > 1) {
                        accBlWindow.logError("Current image contains more than 1 channel (" + donorCBefore.getNChannels() + "). Please split it into parts. (ct. corr.)");
                        donorCBefore = null;
                        return;
                    } else if (donorCBefore.getNSlices() > 1) {
                        accBlWindow.logError("Current image contains more than 1 slice (" + donorCBefore.getNSlices() + "). Please split it into parts. (ct. corr.)");
                        donorCBefore = null;
                        return;
                    }
                    if (donorCBefore != null && acceptorCBefore != null && donorCBefore.equals(acceptorCBefore)) {
                        accBlWindow.logError("The two images must not be the same. Please select and set an other image. (ct. corr.)");
                        donorCBefore.setTitle("");
                        donorCBefore = null;
                        return;
                    }
                    donorCBefore.setTitle("Donor before bleaching (ct. corr.) - " + new Date().toString());
                    new ImageConverter(donorCBefore).convertToGray32();
                    setDonorBeforeButton.setBackground(accBlWindow.greenColor);
                    setDonorBeforeButton.setOpaque(true);
                    setDonorBeforeButton.setBorderPainted(false);
                    break;
                case "setAcceptorCBefore":
                    acceptorCBefore = WindowManager.getCurrentImage();
                    if (acceptorCBefore == null) {
                        accBlWindow.logError("No image is selected. (ct. corr.)");
                        return;
                    }
                    if (acceptorCBefore.getNChannels() > 1) {
                        accBlWindow.logError("Current image contains more than 1 channel (" + acceptorCBefore.getNChannels() + "). Please split it into parts. (ct. corr.)");
                        acceptorCBefore = null;
                        return;
                    } else if (acceptorCBefore.getNSlices() > 1) {
                        accBlWindow.logError("Current image contains more than 1 slice (" + acceptorCBefore.getNSlices() + "). Please split it into parts. (ct. corr.)");
                        acceptorCBefore = null;
                        return;
                    }
                    if (donorCBefore != null && acceptorCBefore != null && donorCBefore.equals(acceptorCBefore)) {
                        accBlWindow.logError("The two images must not be the same. Please select and set an other image. (ct. corr.)");
                        acceptorCBefore.setTitle("");
                        acceptorCBefore = null;
                        return;
                    }
                    acceptorCBefore.setTitle("Acceptor before bleaching (ct. corr.) - " + new Date().toString());
                    new ImageConverter(acceptorCBefore).convertToGray32();
                    setAcceptorBeforeButton.setBackground(accBlWindow.greenColor);
                    setAcceptorBeforeButton.setOpaque(true);
                    setAcceptorBeforeButton.setBorderPainted(false);
                    break;
                case "subtractDonorCBefore": {
                    if (donorCBefore == null) {
                        accBlWindow.logError("No image is set as donor before bleaching. (ct. corr.)");
                        return;
                    } else if (donorCBefore.getRoi() == null) {
                        accBlWindow.logError("No ROI is defined for donor before bleaching. (ct. corr.)");
                        return;
                    }
                    ImageProcessor ipDB = donorCBefore.getProcessor();
                    int width = donorCBefore.getWidth();
                    int height = donorCBefore.getHeight();
                    double sum = 0;
                    int count = 0;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (donorCBefore.getRoi().contains(i, j)) {
                                sum += ipDB.getPixelValue(i, j);
                                count++;
                            }
                        }
                    }
                    float backgroundAvgDB = (float) (sum / count);
                    float value = 0;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            value = ipDB.getPixelValue(x, y);
                            value = value - backgroundAvgDB;
                            ipDB.putPixelValue(x, y, value);
                        }
                    }
                    donorCBefore.updateAndDraw();
                    donorCBefore.killRoi();
                    accBlWindow.log("Subtracted background (" + backgroundAvgDB + ") of donor before bleaching. (ct. corr.)");
                    subtractDonorBeforeButton.setBackground(accBlWindow.greenColor);
                    subtractDonorBeforeButton.setOpaque(true);
                    subtractDonorBeforeButton.setBorderPainted(false);
                    break;
                }
                case "subtractAcceptorCBefore": {
                    if (acceptorCBefore == null) {
                        accBlWindow.logError("No image is set as acceptor before bleaching. (ct. corr.)");
                        return;
                    } else if (acceptorCBefore.getRoi() == null) {
                        accBlWindow.logError("No ROI is defined for acceptor before bleaching. (ct. corr.)");
                        return;
                    }
                    ImageProcessor ipAB = acceptorCBefore.getProcessor();
                    int width = acceptorCBefore.getWidth();
                    int height = acceptorCBefore.getHeight();
                    double sum = 0;
                    int count = 0;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (acceptorCBefore.getRoi().contains(i, j)) {
                                sum += ipAB.getPixelValue(i, j);
                                count++;
                            }
                        }
                    }
                    float backgroundAvgDA = (float) (sum / count);
                    float value = 0;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            value = ipAB.getPixelValue(x, y);
                            value = value - backgroundAvgDA;
                            ipAB.putPixelValue(x, y, value);
                        }
                    }
                    acceptorCBefore.updateAndDraw();
                    acceptorCBefore.killRoi();
                    accBlWindow.log("Subtracted background (" + backgroundAvgDA + ") of acceptor before bleaching. (ct. corr.)");
                    subtractAcceptorBeforeButton.setBackground(accBlWindow.greenColor);
                    subtractAcceptorBeforeButton.setOpaque(true);
                    subtractAcceptorBeforeButton.setBorderPainted(false);
                    break;
                }
                case "setDonorCBeforeThreshold":
                    if (donorCBefore == null) {
                        accBlWindow.logError("No image is set as donor before bleaching. (ct. corr.)");
                        return;
                    }
                    IJ.selectWindow(donorCBefore.getTitle());
                    IJ.run("Threshold...");
                    setDonorBeforeThresholdButton.setBackground(accBlWindow.greenColor);
                    setDonorBeforeThresholdButton.setOpaque(true);
                    setDonorBeforeThresholdButton.setBorderPainted(false);
                    break;
                case "setAcceptorCBeforeThreshold":
                    if (acceptorCBefore == null) {
                        accBlWindow.logError("No image is set as acceptor before bleaching. (ct. corr.)");
                        return;
                    }
                    IJ.selectWindow(acceptorCBefore.getTitle());
                    IJ.run("Threshold...");
                    setAcceptorBeforeThresholdButton.setBackground(accBlWindow.greenColor);
                    setAcceptorBeforeThresholdButton.setOpaque(true);
                    setAcceptorBeforeThresholdButton.setBorderPainted(false);
                    break;
                case "calculate":
                    if (donorCBefore == null) {
                        accBlWindow.logError("No image is set as donor before bleaching. (ct. corr.)");
                        return;
                    } else if (acceptorCBefore == null) {
                        accBlWindow.logError("No image is set as acceptor before bleaching. (ct. corr.)");
                        return;
                    } else {
                        DecimalFormat df = new DecimalFormat("#.###");
                        ImageProcessor ipDB = donorCBefore.getProcessor();
                        ImageProcessor ipAB = acceptorCBefore.getProcessor();
                        float[][] corrImgPoints = null;
                        int width = ipDB.getWidth();
                        int height = ipDB.getHeight();
                        if (showCTCImagesCB.isSelected()) {
                            corrImgPoints = new float[width][height];
                        }
                        double sumc = 0;
                        double countc = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (ipAB.getPixelValue(i, j) > 0 && ipDB.getPixelValue(i, j) > 0) {
                                    double current = ipDB.getPixelValue(i, j) / ipAB.getPixelValue(i, j);
                                    sumc += current;
                                    countc++;
                                    if (showCTCImagesCB.isSelected()) {
                                        corrImgPoints[i][j] = (float) current;
                                    }
                                }
                            }
                        }
                        float avg = (float) (sumc / countc);
                        mode1ResultLabel.setText(df.format(avg));

                        float[] ipDBP = (float[]) ipDB.getPixels();
                        float[] ipABP = (float[]) ipAB.getPixels();
                        double avgDonorBefore = 0;
                        double avgAcceptorBefore = 0;
                        countc = 0;
                        sumc = 0;
                        for (int i = 0; i < ipDBP.length; i++) {
                            if (ipDBP[i] > 0) {
                                sumc += ipDBP[i];
                                countc++;
                            }
                        }
                        avgDonorBefore = sumc / countc;
                        countc = 0;
                        sumc = 0;
                        for (int i = 0; i < ipABP.length; i++) {
                            if (ipABP[i] > 0) {
                                sumc += ipABP[i];
                                countc++;
                            }
                        }
                        avgAcceptorBefore = sumc / countc;
                        mode2ResultLabel.setText(df.format((float) (avgDonorBefore / avgAcceptorBefore)));
                        calculateButton.setBackground(accBlWindow.greenColor);
                        calculateButton.setOpaque(true);
                        calculateButton.setBorderPainted(false);
                        donorCBefore.changes = false;
                        acceptorCBefore.changes = false;
                        if (showCTCImagesCB.isSelected()) {
                            ImagePlus corrImg = new ImagePlus("Cross-talk correction image", new FloatProcessor(corrImgPoints));
                            corrImg.show();
                        }
                    }
                    break;
                case "setfactor":
                    if (quotientsButton.isSelected()) {
                        if (mode1ResultLabel.getText().equals("")) {
                            accBlWindow.logError("The correction factor has to be calculated before setting it. (ct. corr.)");
                            return;
                        }
                        accBlWindow.setCrosstalkCorrection(mode1ResultLabel.getText());
                        accBlWindow.calculateAccCTCorrButton.setBackground(accBlWindow.greenColor);
                        accBlWindow.calculateAccCTCorrButton.setOpaque(true);
                        accBlWindow.calculateAccCTCorrButton.setBorderPainted(false);
                        setButton.setBackground(accBlWindow.greenColor);
                        setButton.setOpaque(true);
                        setButton.setBorderPainted(false);
                    } else {
                        if (mode2ResultLabel.getText().equals("")) {
                            accBlWindow.logError("The correction factor has to be calculated before setting it. (ct. corr.)");
                            return;
                        }
                        accBlWindow.setCrosstalkCorrection(mode2ResultLabel.getText());
                        accBlWindow.calculateAccCTCorrButton.setBackground(accBlWindow.greenColor);
                        accBlWindow.calculateAccCTCorrButton.setOpaque(true);
                        accBlWindow.calculateAccCTCorrButton.setBorderPainted(false);
                        setButton.setBackground(accBlWindow.greenColor);
                        setButton.setOpaque(true);
                        setButton.setBorderPainted(false);
                    }
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            accBlWindow.logException(t.toString(), t);
        }
    }
}
