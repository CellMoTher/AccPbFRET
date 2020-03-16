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
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
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
class AcceptorPPCorrDialog extends JDialog implements ActionListener {

    private AccPbFRET_Plugin accBlWindow;
    private ImagePlus donorCAfter;
    private ImagePlus acceptorCBefore;
    private JPanel panel;
    private JButton setDonorAfterButton;
    private JButton setAcceptorBeforeButton;
    private JButton setDonorAfterThresholdButton;
    private JButton setAcceptorBeforeThresholdButton;
    private JButton calculateButton;
    private JButton setButton;
    private JButton subtractDonorAfterButton;
    private JButton subtractAcceptorBeforeButton;
    private JButton resetButton;
    private ButtonGroup buttonGroup;
    private JRadioButton averagesButton;
    private JRadioButton quotientsButton;
    private JLabel mode1ResultLabel;
    private JLabel mode2ResultLabel;
    private JCheckBox showPPImagesCB;
    private final DateTimeFormatter dateTimeFormat;

    public AcceptorPPCorrDialog(AccPbFRET_Plugin accBlWindow) {
        setTitle("Acceptor Photoproduct Correction Factor");
        this.accBlWindow = accBlWindow;
        dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
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
        JLabel infoLabel = new JLabel("<html><center>This factor is calculated based on images of a sample labeled with acceptor only, donor after and acceptor before photobleaching.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridwidth = 3;
        gc.gridx = 0;
        gc.gridy = 1;
        setDonorAfterButton = new JButton("Set donor after bleaching (acc. only)");
        setDonorAfterButton.addActionListener(this);
        setDonorAfterButton.setActionCommand("setDonorCAfter");
        panel.add(setDonorAfterButton, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        setAcceptorBeforeButton = new JButton("Set acceptor before bleaching (acc. only)");
        setAcceptorBeforeButton.addActionListener(this);
        setAcceptorBeforeButton.setActionCommand("setAcceptorCBefore");
        panel.add(setAcceptorBeforeButton, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        subtractDonorAfterButton = new JButton("Subtract background of donor after");
        subtractDonorAfterButton.addActionListener(this);
        subtractDonorAfterButton.setActionCommand("subtractDonorCAfter");
        panel.add(subtractDonorAfterButton, gc);
        gc.gridx = 0;
        gc.gridy = 4;
        subtractAcceptorBeforeButton = new JButton("Subtract background of acceptor before");
        subtractAcceptorBeforeButton.addActionListener(this);
        subtractAcceptorBeforeButton.setActionCommand("subtractAcceptorCBefore");
        panel.add(subtractAcceptorBeforeButton, gc);
        gc.gridx = 0;
        gc.gridy = 5;
        setDonorAfterThresholdButton = new JButton("Set donor after threshold");
        setDonorAfterThresholdButton.addActionListener(this);
        setDonorAfterThresholdButton.setActionCommand("setDonorCAfterThreshold");
        panel.add(setDonorAfterThresholdButton, gc);
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
        quotientsButton.setToolTipText("The factor is the averaged ratio of corresponding pixel values in the donor after and acceptor before photobleaching images.");
        averagesButton = new JRadioButton("Average pixels");
        averagesButton.setToolTipText("The factor is the ratio of the gated pixel averages in the donor after and acceptor before photobleaching images.");
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
        showPPImagesCB = new JCheckBox("Show correction image (for manual calc.)");
        panel.add(showPPImagesCB, gc);
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
                    donorCAfter = null;
                    acceptorCBefore = null;
                    setDonorAfterButton.setBackground(accBlWindow.originalButtonColor);
                    setDonorAfterButton.setOpaque(false);
                    setDonorAfterButton.setBorderPainted(true);
                    setAcceptorBeforeButton.setBackground(accBlWindow.originalButtonColor);
                    setAcceptorBeforeButton.setOpaque(false);
                    setAcceptorBeforeButton.setBorderPainted(true);
                    setDonorAfterThresholdButton.setBackground(accBlWindow.originalButtonColor);
                    setDonorAfterThresholdButton.setOpaque(false);
                    setDonorAfterThresholdButton.setBorderPainted(true);
                    setAcceptorBeforeThresholdButton.setBackground(accBlWindow.originalButtonColor);
                    setAcceptorBeforeThresholdButton.setOpaque(false);
                    setAcceptorBeforeThresholdButton.setBorderPainted(true);
                    calculateButton.setBackground(accBlWindow.originalButtonColor);
                    calculateButton.setOpaque(false);
                    calculateButton.setBorderPainted(true);
                    setButton.setBackground(accBlWindow.originalButtonColor);
                    setButton.setOpaque(false);
                    setButton.setBorderPainted(true);
                    subtractDonorAfterButton.setBackground(accBlWindow.originalButtonColor);
                    subtractDonorAfterButton.setOpaque(false);
                    subtractDonorAfterButton.setBorderPainted(true);
                    subtractAcceptorBeforeButton.setBackground(accBlWindow.originalButtonColor);
                    subtractAcceptorBeforeButton.setOpaque(false);
                    subtractAcceptorBeforeButton.setBorderPainted(true);
                    mode1ResultLabel.setText("");
                    mode2ResultLabel.setText("");
                    break;
                case "setDonorCAfter":
                    donorCAfter = WindowManager.getCurrentImage();
                    if (donorCAfter == null) {
                        accBlWindow.logError("No image is selected. (pp. corr.)");
                        return;
                    }
                    if (donorCAfter.getNChannels() > 1) {
                        accBlWindow.logError("Current image contains more than 1 channel (" + donorCAfter.getNChannels() + "). Please split it into parts. (pp. corr.)");
                        donorCAfter = null;
                        return;
                    } else if (donorCAfter.getNSlices() > 1) {
                        accBlWindow.logError("Current image contains more than 1 slice (" + donorCAfter.getNSlices() + "). Please split it into parts. (pp. corr.)");
                        donorCAfter = null;
                        return;
                    }
                    if (donorCAfter != null && acceptorCBefore != null && donorCAfter.equals(acceptorCBefore)) {
                        accBlWindow.logError("The two images must not be the same. Please select and set an other image. (pp. corr.)");
                        donorCAfter.setTitle("");
                        donorCAfter = null;
                        return;
                    }
                    donorCAfter.setTitle("Donor after bleaching (pp. corr.) - " + dateTimeFormat.format(OffsetDateTime.now()));
                    new ImageConverter(donorCAfter).convertToGray32();
                    setDonorAfterButton.setBackground(accBlWindow.greenColor);
                    setDonorAfterButton.setOpaque(true);
                    setDonorAfterButton.setBorderPainted(false);
                    break;
                case "setAcceptorCBefore":
                    acceptorCBefore = WindowManager.getCurrentImage();
                    if (acceptorCBefore == null) {
                        accBlWindow.logError("No image is selected. (pp. corr.)");
                        return;
                    }
                    if (acceptorCBefore.getNChannels() > 1) {
                        accBlWindow.logError("Current image contains more than 1 channel (" + acceptorCBefore.getNChannels() + "). Please split it into parts. (pp. corr.)");
                        acceptorCBefore = null;
                        return;
                    } else if (acceptorCBefore.getNSlices() > 1) {
                        accBlWindow.logError("Current image contains more than 1 slice (" + acceptorCBefore.getNSlices() + "). Please split it into parts. (pp. corr.)");
                        acceptorCBefore = null;
                        return;
                    }
                    if (donorCAfter != null && acceptorCBefore != null && donorCAfter.equals(acceptorCBefore)) {
                        accBlWindow.logError("The two images must not be the same. Please select and set an other image. (pp. corr.)");
                        acceptorCBefore.setTitle("");
                        acceptorCBefore = null;
                        return;
                    }
                    acceptorCBefore.setTitle("Acceptor before bleaching (pp. corr.) - " + dateTimeFormat.format(OffsetDateTime.now()));
                    new ImageConverter(acceptorCBefore).convertToGray32();
                    setAcceptorBeforeButton.setBackground(accBlWindow.greenColor);
                    setAcceptorBeforeButton.setOpaque(true);
                    setAcceptorBeforeButton.setBorderPainted(false);
                    break;
                case "subtractDonorCAfter": {
                    if (donorCAfter == null) {
                        accBlWindow.logError("No image is set as donor after bleaching. (pp. corr.)");
                        return;
                    } else if (donorCAfter.getRoi() == null) {
                        accBlWindow.logError("No ROI is defined for donor after bleaching. (pp. corr.)");
                        return;
                    }
                    ImageProcessor ipDA = donorCAfter.getProcessor();
                    int width = donorCAfter.getWidth();
                    int height = donorCAfter.getHeight();
                    double sum = 0;
                    int count = 0;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (donorCAfter.getRoi().contains(i, j)) {
                                sum += ipDA.getPixelValue(i, j);
                                count++;
                            }
                        }
                    }
                    float backgroundAvgDB = (float) (sum / count);
                    float value = 0;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            value = ipDA.getPixelValue(x, y);
                            value = value - backgroundAvgDB;
                            ipDA.putPixelValue(x, y, value);
                        }
                    }
                    donorCAfter.updateAndDraw();
                    donorCAfter.killRoi();
                    accBlWindow.log("Subtracted background (" + backgroundAvgDB + ") of donor after bleaching. (pp. corr.)");
                    subtractDonorAfterButton.setBackground(accBlWindow.greenColor);
                    subtractDonorAfterButton.setOpaque(true);
                    subtractDonorAfterButton.setBorderPainted(false);
                    break;
                }
                case "subtractAcceptorCBefore": {
                    if (acceptorCBefore == null) {
                        accBlWindow.logError("No image is set as acceptor before bleaching. (pp. corr.)");
                        return;
                    } else if (acceptorCBefore.getRoi() == null) {
                        accBlWindow.logError("No ROI is defined for acceptor before bleaching. (pp. corr.)");
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
                    accBlWindow.log("Subtracted background (" + backgroundAvgDA + ") of acceptor before bleaching. (pp. corr.)");
                    subtractAcceptorBeforeButton.setBackground(accBlWindow.greenColor);
                    subtractAcceptorBeforeButton.setOpaque(true);
                    subtractAcceptorBeforeButton.setBorderPainted(false);
                    break;
                }
                case "setDonorCAfterThreshold":
                    if (donorCAfter == null) {
                        accBlWindow.logError("No image is set as donor after bleaching. (pp. corr.)");
                        return;
                    }
                    IJ.selectWindow(donorCAfter.getTitle());
                    IJ.run("Threshold...");
                    setDonorAfterThresholdButton.setBackground(accBlWindow.greenColor);
                    setDonorAfterThresholdButton.setOpaque(true);
                    setDonorAfterThresholdButton.setBorderPainted(false);
                    break;
                case "setAcceptorCBeforeThreshold":
                    if (acceptorCBefore == null) {
                        accBlWindow.logError("No image is set as acceptor before bleaching. (pp. corr.)");
                        return;
                    }
                    IJ.selectWindow(acceptorCBefore.getTitle());
                    IJ.run("Threshold...");
                    setAcceptorBeforeThresholdButton.setBackground(accBlWindow.greenColor);
                    setAcceptorBeforeThresholdButton.setOpaque(true);
                    setAcceptorBeforeThresholdButton.setBorderPainted(false);
                    break;
                case "calculate":
                    if (donorCAfter == null) {
                        accBlWindow.logError("No image is set as donor after bleaching. (pp. corr.)");
                        return;
                    } else if (acceptorCBefore == null) {
                        accBlWindow.logError("No image is set as acceptor before bleaching. (pp. corr.)");
                        return;
                    } else {
                        DecimalFormat df = new DecimalFormat("#.###");
                        ImageProcessor ipDA = donorCAfter.getProcessor();
                        ImageProcessor ipAB = acceptorCBefore.getProcessor();
                        float[][] corrImgPoints = null;
                        int width = ipDA.getWidth();
                        int height = ipDA.getHeight();
                        if (showPPImagesCB.isSelected()) {
                            corrImgPoints = new float[width][height];
                        }
                        double sumc = 0;
                        double countc = 0;
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (ipAB.getPixelValue(i, j) > 0 && ipDA.getPixelValue(i, j) > 0) {
                                    double current = ipDA.getPixelValue(i, j) / ipAB.getPixelValue(i, j);
                                    sumc += current;
                                    countc++;
                                    if (showPPImagesCB.isSelected()) {
                                        corrImgPoints[i][j] = (float) current;
                                    }
                                }
                            }
                        }
                        float avg = (float) (sumc / countc);
                        mode1ResultLabel.setText(df.format(avg));

                        float[] ipDAP = (float[]) ipDA.getPixels();
                        float[] ipABP = (float[]) ipAB.getPixels();
                        double avgDonorAfter = 0;
                        double avgAcceptorBefore = 0;
                        countc = 0;
                        sumc = 0;
                        for (int i = 0; i < ipDAP.length; i++) {
                            if (ipDAP[i] > 0) {
                                sumc += ipDAP[i];
                                countc++;
                            }
                        }
                        avgDonorAfter = sumc / countc;
                        countc = 0;
                        sumc = 0;
                        for (int i = 0; i < ipABP.length; i++) {
                            if (ipABP[i] > 0) {
                                sumc += ipABP[i];
                                countc++;
                            }
                        }
                        avgAcceptorBefore = sumc / countc;
                        mode2ResultLabel.setText(df.format((float) (avgDonorAfter / avgAcceptorBefore)));
                        calculateButton.setBackground(accBlWindow.greenColor);
                        calculateButton.setOpaque(true);
                        calculateButton.setBorderPainted(false);
                        donorCAfter.changes = false;
                        acceptorCBefore.changes = false;
                        if (showPPImagesCB.isSelected()) {
                            ImagePlus corrImg = new ImagePlus("Photoproduct correction image", new FloatProcessor(corrImgPoints));
                            corrImg.show();
                        }
                    }
                    break;
                case "setfactor":
                    if (quotientsButton.isSelected()) {
                        if (mode1ResultLabel.getText().equals("")) {
                            accBlWindow.logError("The correction factor has to be calculated before setting it. (pp. corr.)");
                            return;
                        }
                        accBlWindow.setPhotoproductCorrection(mode1ResultLabel.getText());
                        setButton.setBackground(accBlWindow.greenColor);
                        setButton.setOpaque(true);
                        setButton.setBorderPainted(false);
                        accBlWindow.calculateAccPPCorrButton.setBackground(accBlWindow.greenColor);
                        accBlWindow.calculateAccPPCorrButton.setOpaque(true);
                        accBlWindow.calculateAccPPCorrButton.setBorderPainted(false);
                    } else {
                        if (mode2ResultLabel.getText().equals("")) {
                            accBlWindow.logError("The correction factor has to be calculated before setting it. (pp. corr.)");
                            return;
                        }
                        accBlWindow.setPhotoproductCorrection(mode2ResultLabel.getText());
                        setButton.setBackground(accBlWindow.greenColor);
                        setButton.setOpaque(true);
                        setButton.setBorderPainted(false);
                        accBlWindow.calculateAccPPCorrButton.setBackground(accBlWindow.greenColor);
                        accBlWindow.calculateAccPPCorrButton.setOpaque(true);
                        accBlWindow.calculateAccPPCorrButton.setBorderPainted(false);
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
