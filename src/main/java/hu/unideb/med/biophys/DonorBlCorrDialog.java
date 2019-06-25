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

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.FHT;
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
public class DonorBlCorrDialog extends JDialog implements ActionListener{
    private AccPbFRET_Plugin accBlWindow;
    private ImagePlus donorCBefore, donorCAfter;
    private JPanel panel;
    private JButton setBeforeButton, setAfterButton, registerButton;
    private JButton setBeforeThresholdButton, setAfterThresholdButton, calculateButton, setButton;
    private JButton subtractBeforeButton, subtractAfterButton;
    private JButton resetButton;
    private ButtonGroup buttonGroup;
    private JRadioButton averagesButton, quotientsButton;
    private JLabel mode1ResultLabel, mode2ResultLabel;
    private JCheckBox showBlCImagesCB;

    public DonorBlCorrDialog(AccPbFRET_Plugin accBlWindow) {
        setTitle("Donor bleaching correction factor");
        this.accBlWindow = accBlWindow;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(300, 410);
        setLocation((screen.width - getWidth())/2, (screen.height - getHeight())/2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(2,2,6,2);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 0;
        JLabel infoLabel = new JLabel("<html><center>This factor is calculated based on images of the donor channel of a donor only labeled sample, before and after photobleaching.</center></html>");
        panel.add(infoLabel, gc);
        gc.insets = new Insets(2,2,2,2);
        gc.gridwidth = 3;
        gc.gridx = 0;
        gc.gridy = 1;
        setBeforeButton = new JButton("Set donor before bleaching (donor only)");
        setBeforeButton.addActionListener(this);
        setBeforeButton.setActionCommand("setCBefore");
        panel.add(setBeforeButton, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        setAfterButton = new JButton("Set donor after bleaching (donor only)");
        setAfterButton.addActionListener(this);
        setAfterButton.setActionCommand("setCAfter");
        panel.add(setAfterButton, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        registerButton = new JButton("Register images");
        registerButton.addActionListener(this);
        registerButton.setActionCommand("registerCImages");
        panel.add(registerButton, gc);
        gc.gridx = 0;
        gc.gridy = 4;
        subtractBeforeButton = new JButton("Subtract background of donor before");
        subtractBeforeButton.addActionListener(this);
        subtractBeforeButton.setActionCommand("subtractCBefore");
        panel.add(subtractBeforeButton, gc);
        gc.gridx = 0;
        gc.gridy = 5;
        subtractAfterButton = new JButton("Subtract background of donor after");
        subtractAfterButton.addActionListener(this);
        subtractAfterButton.setActionCommand("subtractCAfter");
        panel.add(subtractAfterButton, gc);
        gc.gridx = 0;
        gc.gridy = 6;
        setBeforeThresholdButton = new JButton("Set donor before threshold");
        setBeforeThresholdButton.addActionListener(this);
        setBeforeThresholdButton.setActionCommand("setCBeforeThreshold");
        panel.add(setBeforeThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 7;
        setAfterThresholdButton = new JButton("Set donor after threshold");
        setAfterThresholdButton.addActionListener(this);
        setAfterThresholdButton.setActionCommand("setCAfterThreshold");
        panel.add(setAfterThresholdButton, gc);
        gc.gridx = 0;
        gc.gridy = 8;
        gc.gridheight = 2;
        JPanel radioPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gcr = new GridBagConstraints();
        radioPanel.setLayout(gbl);
        gcr.insets = new Insets(0,4,4,4);
        gcr.fill = GridBagConstraints.BOTH;
        JLabel modeLabel = new JLabel("Mode:");
        JLabel resultLabel = new JLabel("Result:");
        mode1ResultLabel = new JLabel("", JLabel.CENTER);
        mode2ResultLabel = new JLabel("", JLabel.CENTER);
        quotientsButton = new JRadioButton("point-by-point");
        quotientsButton.setToolTipText("The factor is the averaged ratio of corresponding pixel values in the donor before and after photobleaching images.");
        averagesButton = new JRadioButton("average pixels");
        averagesButton.setToolTipText("The factor is the ratio of the gated pixel averages in the donor before and after photobleaching images.");
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
        gc.gridy = 10;
        gc.gridheight = 1;
        showBlCImagesCB = new JCheckBox("show correction image (for manual calc.)");
        panel.add(showBlCImagesCB, gc);
        gc.gridwidth = 1;
        gc.gridheight = 1;
        gc.gridx = 0;
        gc.gridy = 11;
        calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(this);
        calculateButton.setActionCommand("calculate");
        panel.add(calculateButton, gc);
        gc.gridx = 1;
        gc.gridy = 11;
        setButton = new JButton("Set selected");
        setButton.addActionListener(this);
        setButton.setActionCommand("setfactor");
        panel.add(setButton, gc);
        gc.gridx = 2;
        gc.gridy = 11;
        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        resetButton.setActionCommand("reset");
        panel.add(resetButton, gc);

        getContentPane().add(panel);
    }

    public void actionPerformed(ActionEvent e) {
    	try {
            if (e.getActionCommand().equals("reset")) {
                donorCBefore = null;
                donorCAfter = null;
	            setBeforeButton.setBackground(accBlWindow.originalButtonColor);
	            setAfterButton.setBackground(accBlWindow.originalButtonColor);
	            setBeforeThresholdButton.setBackground(accBlWindow.originalButtonColor);
                setAfterThresholdButton.setBackground(accBlWindow.originalButtonColor);
                calculateButton.setBackground(accBlWindow.originalButtonColor);
                setButton.setBackground(accBlWindow.originalButtonColor);
                subtractBeforeButton.setBackground(accBlWindow.originalButtonColor);
                subtractAfterButton.setBackground(accBlWindow.originalButtonColor);
                registerButton.setBackground(accBlWindow.originalButtonColor);
                mode1ResultLabel.setText("");
                mode2ResultLabel.setText("");
      	    } else if (e.getActionCommand().equals("setCBefore")) {
                donorCBefore = WindowManager.getCurrentImage();
      	        if (donorCBefore == null) {
                    accBlWindow.logError("No image is selected. (bl. corr.)");
                    return;
                }
                if (donorCBefore.getNChannels() > 1) {
                   accBlWindow.logError("Current image contains more than 1 channel ("+donorCBefore.getNChannels()+"). Please split it into parts. (bl. corr.)");
                   donorCBefore = null;
                   return;
                } else if (donorCBefore.getNSlices() > 1) {
                   accBlWindow.logError("Current image contains more than 1 slice ("+donorCBefore.getNSlices()+"). Please split it into parts. (bl. corr.)");
                   donorCBefore = null;
                   return;
                }
                if (donorCBefore != null && donorCAfter != null && donorCBefore.equals(donorCAfter)) {
                    accBlWindow.logError("The two images must not be the same. Please select and set an other image. (bl. corr.)");
                    donorCBefore.setTitle("");
                    donorCBefore = null;
                    return;
                }
                donorCBefore.setTitle("Donor before bleaching (bl. corr.) - " + new Date().toString());
                new ImageConverter(donorCBefore).convertToGray32();
                setBeforeButton.setBackground(accBlWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setCAfter")) {
                donorCAfter = WindowManager.getCurrentImage();
      	        if (donorCAfter == null) {
                    accBlWindow.logError("No image is selected. (bl. corr.)");
                    return;
                }
                if (donorCAfter.getNChannels() > 1) {
                   accBlWindow.logError("Current image contains more than 1 channel ("+donorCAfter.getNChannels()+"). Please split it into parts. (bl. corr.)");
                   donorCAfter = null;
                   return;
                } else if (donorCAfter.getNSlices() > 1) {
                   accBlWindow.logError("Current image contains more than 1 slice ("+donorCAfter.getNSlices()+"). Please split it into parts. (bl. corr.)");
                   donorCAfter = null;
                   return;
                }
                if (donorCBefore != null && donorCAfter != null && donorCBefore.equals(donorCAfter)) {
                    accBlWindow.logError("The two images must not be the same. Please select and set an other image. (bl. corr.)");
                    donorCAfter.setTitle("");
                    donorCAfter = null;
                    return;
                }
                donorCAfter.setTitle("Donor after bleaching (bl. corr.) - " + new Date().toString());
                new ImageConverter(donorCAfter).convertToGray32();
                setAfterButton.setBackground(accBlWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractCBefore")) {
      	        if (donorCBefore == null) {
                    accBlWindow.logError("No image is set as donor before bleaching. (bl. corr.)");
                    return;
                } else if (donorCBefore.getRoi() == null) {
                    accBlWindow.logError("No ROI is defined for donor before bleaching. (bl. corr.)");
                    return;
                }
                ImageProcessor ipDB = donorCBefore.getProcessor();
                int width = donorCBefore.getWidth();
                int height = donorCBefore.getHeight();
                double sum = 0;
                int count = 0;
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (donorCBefore.getRoi().contains(i, j)) {
                            sum += ipDB.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgDB = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipDB.getPixelValue(x,y);
                        value = value - backgroundAvgDB;
		                ipDB.putPixelValue(x, y, value);
        		    }
		        }
		        donorCBefore.updateAndDraw();
		        donorCBefore.killRoi();
                accBlWindow.log("Subtracted background ("+backgroundAvgDB+") of donor before bleaching. (bl. corr.)");
                subtractBeforeButton.setBackground(accBlWindow.greenColor);
      	    } else if (e.getActionCommand().equals("subtractCAfter")) {
      	        if (donorCAfter == null) {
                    accBlWindow.logError("No image is set as donor after bleaching. (bl. corr.)");
                    return;
                } else if (donorCAfter.getRoi() == null) {
                    accBlWindow.logError("No ROI is defined for donor after bleaching. (bl. corr.)");
                    return;
                }
                ImageProcessor ipDA = donorCAfter.getProcessor();
                int width = donorCAfter.getWidth();
                int height = donorCAfter.getHeight();
                double sum = 0;
                int count = 0;
                for (int i=0; i<width; i++) {
                    for (int j=0; j<height; j++) {
                        if (donorCAfter.getRoi().contains(i, j)) {
                            sum += ipDA.getPixelValue(i,j);
                            count++;
                        }
		            }
        		}
		        float backgroundAvgDA = (float)(sum/count);

                float value = 0;
                for (int x=0; x < width; x++) {
                    for (int y=0; y < height; y++) {
                        value = ipDA.getPixelValue(x,y);
                        value = value - backgroundAvgDA;
		                ipDA.putPixelValue(x, y, value);
        		    }
		        }
		        donorCAfter.updateAndDraw();
		        donorCAfter.killRoi();
                accBlWindow.log("Subtracted background ("+backgroundAvgDA+") of donor after bleaching. (bl. corr.)");
                subtractAfterButton.setBackground(accBlWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setCBeforeThreshold")) {
      	        if (donorCBefore == null) {
                    accBlWindow.logError("No image is set as donor before bleaching. (bl. corr.)");
                    return;
                }
                IJ.selectWindow(donorCBefore.getTitle());
                IJ.run("Threshold...");
                setBeforeThresholdButton.setBackground(accBlWindow.greenColor);
      	    } else if (e.getActionCommand().equals("setCAfterThreshold")) {
      	        if (donorCAfter == null) {
                    accBlWindow.logError("No image is set as donor after bleaching. (bl. corr.)");
                    return;
                }
                IJ.selectWindow(donorCAfter.getTitle());
                IJ.run("Threshold...");
                setAfterThresholdButton.setBackground(accBlWindow.greenColor);
      	    } else if (e.getActionCommand().equals("calculate")) {
                if (donorCBefore == null) {
                    accBlWindow.logError("No image is set as donor before bleaching. (bl. corr.)");
                    return;
      	        } else if (donorCAfter == null) {
                    accBlWindow.logError("No image is set as donor after bleaching. (bl. corr.)");
                    return;
                } else {
                    DecimalFormat df = new DecimalFormat("#.###");
                    ImageProcessor ipDB = donorCBefore.getProcessor();
                    ImageProcessor ipDA = donorCAfter.getProcessor();
                    float[][] corrImgPoints = null;
                    int width = ipDB.getWidth();
                    int height = ipDB.getHeight();
                    if(showBlCImagesCB.isSelected()) {
                        corrImgPoints = new float[width][height];
                    }
                    double sumc = 0;
                    double countc = 0;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (ipDA.getPixelValue(i, j) > 0 && ipDB.getPixelValue(i, j) > 0) {
                                double current = ipDB.getPixelValue(i, j) / ipDA.getPixelValue(i, j);
                                sumc += current;
                                countc++;
                                if(showBlCImagesCB.isSelected()) {
                                    corrImgPoints[i][j] = (float)current;
                                }
                            }
                        }
                    }
                    float avg = (float)(sumc / countc);
                    mode1ResultLabel.setText(df.format(avg).toString());

                    float[] ipDBP = (float[])ipDB.getPixels();
                    float[] ipDAP = (float[])ipDA.getPixels();
                    double avgBefore = 0;
                    double avgAfter = 0;
                    countc = 0;
                    sumc = 0;
                    for (int i = 0; i < ipDBP.length; i++) {
                        if (ipDBP[i] > 0) {
                            sumc += ipDBP[i];
                            countc++;
                        }
                    }
                    avgBefore = sumc / countc;
                    countc = 0;
                    sumc = 0;
                    for (int i = 0; i < ipDAP.length; i++) {
                        if (ipDAP[i] > 0) {
                            sumc += ipDAP[i];
                            countc++;
                        }
                    }
                    avgAfter = sumc / countc;
                    mode2ResultLabel.setText(df.format((float)(avgBefore/avgAfter)).toString());
                    calculateButton.setBackground(accBlWindow.greenColor);
                    donorCBefore.changes = false;
                    donorCAfter.changes = false;
                    if(showBlCImagesCB.isSelected()) {
                        ImagePlus corrImg = new ImagePlus("Donor bleaching correction image", new FloatProcessor(corrImgPoints));
                        corrImg.show();
                    }
                }
      	    } else if (e.getActionCommand().equals("setfactor")) {
                    if (quotientsButton.isSelected()) {
                        if (mode1ResultLabel.getText().equals("")) {
                           accBlWindow.logError("The correction factor has to be calculated before setting it. (bl. corr.)");
                           return;
                        }
                        accBlWindow.setBleachingCorrection(mode1ResultLabel.getText());
                        setButton.setBackground(accBlWindow.greenColor);
                        accBlWindow.calculateDBCorrButton.setBackground(accBlWindow.greenColor);
                    } else {
                        if (mode2ResultLabel.getText().equals("")) {
                           accBlWindow.logError("The correction factor has to be calculated before setting it. (bl. corr.)");
                           return;
                        }
                        accBlWindow.setBleachingCorrection(mode2ResultLabel.getText());
                        setButton.setBackground(accBlWindow.greenColor);
                        accBlWindow.calculateDBCorrButton.setBackground(accBlWindow.greenColor);
                    }
      	    } else if (e.getActionCommand().equals("registerCImages")) {
      	        if (donorCBefore == null) {
                    accBlWindow.logError("No image is set as donor before bleaching. (bl. corr.)");
                    return;
                } else if (donorCAfter == null) {
                    accBlWindow.logError("No image is set as donor after bleaching. (bl. corr.)");
                    return;
                } else {
                    FHT fht1 = new FHT(donorCBefore.getProcessor().duplicate());
                    fht1.transform();
                    FHT fht2 = new FHT(donorCAfter.getProcessor().duplicate());
                    fht2.transform();
                    FHT res = fht1.conjugateMultiply(fht2);
                    res.inverseTransform();
                    ImagePlus image = new ImagePlus("Result of registration", res);
                    ImageProcessor ip = image.getProcessor();
                    int width = ip.getWidth();
                    int height = ip.getHeight();
                    int maximum = 0;
                    int maxx = -1;
                    int maxy = -1;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (ip.getPixel(i, j) > maximum) {
                                maximum = ip.getPixel(i, j);
                                maxx = i;
                                maxy = j;
                            }
                        }
                    }
                    int shiftX = 0;
                    int shiftY = 0;
                    if (maxx != 0 || maxy != 0){
                        ShiftDialog sd = new ShiftDialog(accBlWindow);
                        if (maxy > height/2) {
                            accBlWindow.log("Shifting donor after image up " + (height-maxy) + " pixel" + ((height-maxy)>1?"s":"") + ". (bl. corr.)");
                            sd.shiftUp(donorCAfter, height-maxy);
                        } else if (maxy != 0) {
                            accBlWindow.log("Shifting donor after image down " + maxy + " pixel" + (maxy>1?"s":"") + ". (bl. corr.)");
                            sd.shiftDown(donorCAfter, maxy);
                        }
                        if (maxx > width/2) {
                            accBlWindow.log("Shifting donor after image to the left " + (width-maxx) + " pixel" + ((width-maxx)>1?"s":"") + ". (bl. corr.)");
                            sd.shiftLeft(donorCAfter, width-maxx);
                        } else if (maxx != 0) {
                            accBlWindow.log("Shifting donor after image to the right " + maxx + " pixel" + (maxx>1?"s":"") + ". (bl. corr.)");
                            sd.shiftRight(donorCAfter, maxx);
                        }
                        actionPerformed(new ActionEvent(registerButton, 1, "registerCImages"));
                    } else {
                        accBlWindow.log("Registration finished. Maximum: x=" + maxx + " y=" + maxy + " (bl. corr.)");
                        registerButton.setBackground(accBlWindow.greenColor);
                    }
                }
            }
        } catch (Throwable t) {
            accBlWindow.logException(t.toString(), t);
        }
    }
}
