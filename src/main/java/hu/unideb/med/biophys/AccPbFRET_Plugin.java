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
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.StackEditor;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.GaussianBlur;
import ij.process.FHT;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.ColorModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ToolTipManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class AccPbFRET_Plugin extends JFrame implements ActionListener, WindowListener {

    private final float version = 3.16F;
    private final String lastModified = "24 May 2010";
    private final String imageJVersion = "1.42k";
    private final String javaVersion = "1.6.0_05";
    private int windowWidth = 650;
    private int windowHeight = 890;
    private ImagePlus donorBefore, donorAfter, acceptorBefore, acceptorAfter, transferImage = null;
    private ImageProcessor donorBeforeSave = null, donorAfterSave = null, acceptorBeforeSave = null, acceptorAfterSave = null;
    private ResultsTable resultsTable;
    private Analyzer analyzer;
    private ApplyMaskDialog applyMaskDialog;
    private CalculateImgRatioDialog calculateImgRatioDialog;
    private ShiftDialog shiftDialog;
    private DonorBlCorrDialog donorBlCorrDialog;
    private AcceptorCTCorrDialog acceptorCTCorrDialog;
    private AcceptorPPCorrDialog acceptorPPCorrDialog;
    private JMenuBar menuBar;
    private HelpWindow helpWindow;
    private JMenu fileMenu, imageMenu, correctionMenu, helpMenu;
    private JMenuItem openMenuItem, saveTiffMenuItem, saveBmpMenuItem, splitMenuItem, applyMaskMenuItem, bleachingMaskMenuItem, calculateImgRatioMenuItem, thresholdMenuItem, shiftMenuItem;
    private JMenuItem lutFireMenuItem, lutSpectrumMenuItem, histogramMenuItem, convertMenuItem, exitMenuItem, helpMenuItem, aboutMenuItem, checkVersionMenuItem;
    private JMenuItem saveMessagesMenuItem, clearMessagesMenuItem;
    private JMenuItem semiAutomaticMenuItem, resetImagesMenuItem;
    private JCheckBoxMenuItem donorBlCorrMenuItem, accCrossTalkCorrMenuItem, accPhotoprCorrMenuItem, partialBlCorrMenuItem;
    private JCheckBoxMenuItem debugMenuItem;
    private JButton setDonorBeforeButton, setDonorAfterButton, setAcceptorBeforeButton, setAcceptorAfterButton;
    private JButton subtractDonorBeforeButton, subtractDonorAfterButton, subtractAcceptorBeforeButton, subtractAcceptorAfterButton;
    private JButton thresholdDonorBeforeButton, thresholdDonorAfterButton, thresholdAcceptorBeforeButton, thresholdAcceptorAfterButton;
    private JButton smoothDonorBeforeButton, smoothDonorAfterButton, smoothAcceptorBeforeButton, smoothAcceptorAfterButton;
    private JButton openImageButton, clearABButton, clearAAButton, resetDBButton, resetDAButton, resetABButton, resetAAButton;
    private JButton copyRoiButton;
    private JTextField radiusFieldDB, radiusFieldDA, radiusFieldAB, radiusFieldAA;
    private JTextField donorBlCorrField, accCrossTalkCorrField, accPhotoprCorrField, partialBlCorrField;
    private JButton registerButton, createButton, measureButton, nextButton, closeImagesButton;
    public JButton calculateDBCorrButton, calculateAccCTCorrButton, calculateAccPPCorrButton, calculatePartialBlCorrButton;
    private JPanel lineDonorBlCorr, lineAccCrossTalk, lineAccPhotopr, linePartialBl;
    private JLabel donorBlCorrLabel, accCrossTalkCorrLabel, accPhotoprCorrLabel, partialBlCorrLabel;
    private JCheckBox useLsmImages, useAcceptorAsMask, applyShiftCB;
    private JTextPane log;
    private JScrollPane logScrollPane;
    private SimpleDateFormat format;
    private File[] automaticallyProcessedFiles = null;
    private int currentlyProcessedFile = 0;
    private String currentlyProcessedFileName = null;
    private String currentDirectory = null;
    public Color originalButtonColor = null;
    public Color greenColor = new Color(142, 207, 125);

    public AccPbFRET_Plugin() {
        super();
        setTitle("AccPbFRET v" + version);
        IJ.versionLessThan(imageJVersion);
        Locale.setDefault(Locale.ENGLISH);
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        format = new SimpleDateFormat("HH:mm:ss");
        createGui();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(windowWidth, windowHeight);
        pack();
        setLocation(screen.width - getWidth(), screen.height / 2 - getHeight() / 2);
        setVisible(true);
        currentDirectory = System.getProperty("user.home");
        originalButtonColor = setDonorBeforeButton.getBackground();
        openImageButton.requestFocus();
    }

    public void createGui() {
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        imageMenu = new JMenu("Image");
        menuBar.add(imageMenu);
        correctionMenu = new JMenu("Corrections");
        menuBar.add(correctionMenu);
        helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        openMenuItem = new JMenuItem("Open image");
        openMenuItem.setActionCommand("openImage");
        openMenuItem.addActionListener(this);
        fileMenu.add(openMenuItem);
        saveTiffMenuItem = new JMenuItem("Save image as tiff");
        saveTiffMenuItem.setActionCommand("saveImageAsTiff");
        saveTiffMenuItem.addActionListener(this);
        fileMenu.add(saveTiffMenuItem);
        saveBmpMenuItem = new JMenuItem("Save image as bmp");
        saveBmpMenuItem.setActionCommand("saveImageAsBmp");
        saveBmpMenuItem.addActionListener(this);
        fileMenu.add(saveBmpMenuItem);
        saveMessagesMenuItem = new JMenuItem("Save messages");
        saveMessagesMenuItem.setActionCommand("saveMessages");
        saveMessagesMenuItem.addActionListener(this);
        fileMenu.add(saveMessagesMenuItem);
        clearMessagesMenuItem = new JMenuItem("Clear messages");
        clearMessagesMenuItem.setActionCommand("clearMessages");
        clearMessagesMenuItem.addActionListener(this);
        fileMenu.add(clearMessagesMenuItem);
        semiAutomaticMenuItem = new JMenuItem("Semi-automatic processing");
        semiAutomaticMenuItem.setActionCommand("semiAutomaticProcessing");
        semiAutomaticMenuItem.addActionListener(this);
        fileMenu.add(semiAutomaticMenuItem);
        resetImagesMenuItem = new JMenuItem("Reset all");
        resetImagesMenuItem.setActionCommand("resetImages");
        resetImagesMenuItem.addActionListener(this);
        fileMenu.add(resetImagesMenuItem);
        splitMenuItem = new JMenuItem("Split image");
        splitMenuItem.setActionCommand("split");
        splitMenuItem.addActionListener(this);
        imageMenu.add(splitMenuItem);
        applyMaskMenuItem = new JMenuItem("Apply mask");
        applyMaskMenuItem.setActionCommand("applyMask");
        applyMaskMenuItem.addActionListener(this);
        imageMenu.add(applyMaskMenuItem);
        bleachingMaskMenuItem = new JMenuItem("Create bleached-area mask");
        bleachingMaskMenuItem.setActionCommand("bleachingMask");
        bleachingMaskMenuItem.addActionListener(this);
        imageMenu.add(bleachingMaskMenuItem);
        calculateImgRatioMenuItem = new JMenuItem("Calculate ratio of images");
        calculateImgRatioMenuItem.setActionCommand("calculateRatio");
        calculateImgRatioMenuItem.addActionListener(this);
        imageMenu.add(calculateImgRatioMenuItem);
        convertMenuItem = new JMenuItem("Convert image to 32 bit");
        convertMenuItem.setActionCommand("convertto32bit");
        convertMenuItem.addActionListener(this);
        imageMenu.add(convertMenuItem);
        thresholdMenuItem = new JMenuItem("Set threshold");
        thresholdMenuItem.setActionCommand("threshold");
        thresholdMenuItem.addActionListener(this);
        imageMenu.add(thresholdMenuItem);
        shiftMenuItem = new JMenuItem("Shift image");
        shiftMenuItem.setActionCommand("shiftimage");
        shiftMenuItem.addActionListener(this);
        imageMenu.add(shiftMenuItem);
        histogramMenuItem = new JMenuItem("Histogram");
        histogramMenuItem.setActionCommand("histogram");
        histogramMenuItem.addActionListener(this);
        imageMenu.add(histogramMenuItem);
        lutFireMenuItem = new JMenuItem("LUT Fire");
        lutFireMenuItem.setActionCommand("lutFire");
        lutFireMenuItem.addActionListener(this);
        imageMenu.add(lutFireMenuItem);
        lutSpectrumMenuItem = new JMenuItem("LUT Spectrum");
        lutSpectrumMenuItem.setActionCommand("lutSpectrum");
        lutSpectrumMenuItem.addActionListener(this);
        imageMenu.add(lutSpectrumMenuItem);
        donorBlCorrMenuItem = new JCheckBoxMenuItem("Donor bleaching");
        donorBlCorrMenuItem.setSelected(true);
        donorBlCorrMenuItem.setActionCommand("donorblcorrm");
        donorBlCorrMenuItem.addActionListener(this);
        correctionMenu.add(donorBlCorrMenuItem);
        accCrossTalkCorrMenuItem = new JCheckBoxMenuItem("Acceptor cross-talk");
        accCrossTalkCorrMenuItem.setSelected(false);
        accCrossTalkCorrMenuItem.setActionCommand("acccrtalkcorrm");
        accCrossTalkCorrMenuItem.addActionListener(this);
        correctionMenu.add(accCrossTalkCorrMenuItem);
        accPhotoprCorrMenuItem = new JCheckBoxMenuItem("Acceptor photoproduct");
        accPhotoprCorrMenuItem.setSelected(false);
        accPhotoprCorrMenuItem.setActionCommand("accphprcorrm");
        accPhotoprCorrMenuItem.addActionListener(this);
        correctionMenu.add(accPhotoprCorrMenuItem);
        partialBlCorrMenuItem = new JCheckBoxMenuItem("Partial acceptor photobleaching");
        partialBlCorrMenuItem.setSelected(false);
        partialBlCorrMenuItem.setActionCommand("partialblcorrm");
        partialBlCorrMenuItem.addActionListener(this);
        correctionMenu.add(partialBlCorrMenuItem);
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setActionCommand("exit");
        exitMenuItem.addActionListener(this);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        helpMenuItem = new JMenuItem("Help");
        helpMenuItem.setActionCommand("help");
        helpMenuItem.addActionListener(this);
        helpMenu.add(helpMenuItem);
        checkVersionMenuItem = new JMenuItem("Check for latest version");
        checkVersionMenuItem.setActionCommand("checkVersion");
        checkVersionMenuItem.addActionListener(this);
        helpMenu.add(checkVersionMenuItem);
        debugMenuItem = new JCheckBoxMenuItem("Debug mode");
        debugMenuItem.setSelected(false);
        debugMenuItem.setActionCommand("debugmode");
        debugMenuItem.addActionListener(this);
        helpMenu.add(debugMenuItem);
        aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setActionCommand("about");
        aboutMenuItem.addActionListener(this);
        helpMenu.add(aboutMenuItem);
        setJMenuBar(menuBar);
        addWindowListener(this);
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        Container container = getContentPane();
        container.setLayout(gridbaglayout);

        JPanel donorBeforeBleachingPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        donorBeforeBleachingPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gc.anchor = GridBagConstraints.WEST;
        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(2, 2, 2, 2);
        gc.fill = GridBagConstraints.NONE;
        useLsmImages = new JCheckBox("use LSM", true);
        useLsmImages.setSelected(false);
        useLsmImages.setActionCommand("useLsmImages");
        useLsmImages.addActionListener(this);
        useLsmImages.setToolTipText("<html>If this checkbox is checked, the LSM image containing donor and<BR>acceptor channel images (both before and after photobleaching)<BR> are set automatically after opening. Every previously opened image<br>window will be closed. The results window can be left opened.</html>");
        useLsmImages.setSelected(false);
        donorBeforeBleachingPanel.add(new JLabel("Step 1a: open and set the donor before bleaching image  "));
        donorBeforeBleachingPanel.add(useLsmImages);
        setDonorBeforeButton = new JButton("Set image");
        setDonorBeforeButton.setMargin(new Insets(2, 2, 2, 2));
        setDonorBeforeButton.addActionListener(this);
        setDonorBeforeButton.setActionCommand("setDonorBefore");
        container.add(donorBeforeBleachingPanel, gc);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 0, 2);
        openImageButton = new JButton("Open");
        openImageButton.setToolTipText("Opens an arbitrary image.");
        openImageButton.setMargin(new Insets(0, 0, 0, 0));
        openImageButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        openImageButton.addActionListener(this);
        openImageButton.setActionCommand("openImage");
        container.add(openImageButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 0;
        gc.insets = new Insets(2, 2, 2, 2);
        container.add(setDonorBeforeButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 1;
        container.add(new JLabel("Step 1b: open and set the donor after bleaching image"), gc);
        setDonorAfterButton = new JButton("Set image");
        setDonorAfterButton.setMargin(new Insets(2, 2, 2, 2));
        setDonorAfterButton.addActionListener(this);
        setDonorAfterButton.setActionCommand("setDonorAfter");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 1;
        gc.insets = new Insets(2, 2, 2, 2);
        container.add(setDonorAfterButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 2;
        container.add(new JLabel("Step 1c (optional): open and set acceptor before bleaching image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 2;
        gc.insets = new Insets(0, 0, 0, 2);
        clearABButton = new JButton("Clear");
        clearABButton.setToolTipText("Clears the acceptor before bleaching image.");
        clearABButton.setMargin(new Insets(0, 0, 0, 0));
        clearABButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        clearABButton.addActionListener(this);
        clearABButton.setActionCommand("clearAB");
        container.add(clearABButton, gc);
        setAcceptorBeforeButton = new JButton("Set image");
        setAcceptorBeforeButton.setMargin(new Insets(2, 2, 2, 2));
        setAcceptorBeforeButton.addActionListener(this);
        setAcceptorBeforeButton.setActionCommand("setAcceptorBefore");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 2;
        gc.insets = new Insets(2, 2, 2, 2);
        container.add(setAcceptorBeforeButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 3;
        container.add(new JLabel("Step 1d (optional): open and set acceptor after (partial) bleaching image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 3;
        gc.insets = new Insets(0, 0, 0, 2);
        clearAAButton = new JButton("Clear");
        clearAAButton.setToolTipText("Clears the acceptor after bleaching image.");
        clearAAButton.setMargin(new Insets(0, 0, 0, 0));
        clearAAButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        clearAAButton.addActionListener(this);
        clearAAButton.setActionCommand("clearAA");
        container.add(clearAAButton, gc);
        setAcceptorAfterButton = new JButton("Set image");
        setAcceptorAfterButton.setMargin(new Insets(2, 2, 2, 2));
        setAcceptorAfterButton.addActionListener(this);
        setAcceptorAfterButton.setActionCommand("setAcceptorAfter");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 3;
        gc.insets = new Insets(2, 2, 2, 2);
        container.add(setAcceptorAfterButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 4;
        JPanel line1 = new JPanel();
        line1.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line1.setBackground(Color.lightGray);
        container.add(line1, gc);

        JPanel regPanel = new JPanel();
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 5;
        gc.insets = new Insets(0, 0, 0, 0);
        gc.fill = GridBagConstraints.NONE;
        regPanel.add(new JLabel("Step 2: register donor images  "));
        applyShiftCB = new JCheckBox("apply shift to acceptor image", true);
        applyShiftCB.setSelected(true);
        regPanel.add(applyShiftCB);
        container.add(regPanel, gc);
        registerButton = new JButton("Register");
        registerButton.addActionListener(this);
        registerButton.setActionCommand("registerImages");
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 5;
        container.add(registerButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 6;
        JPanel line2 = new JPanel();
        line2.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line2.setBackground(Color.lightGray);
        container.add(line2, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 7;
        container.add(new JLabel("Step 3a (optional): blur donor before image (Gaussian), radius in pixels:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 7;
        radiusFieldDB = new JTextField("2", 4);
        radiusFieldDB.setHorizontalAlignment(JTextField.RIGHT);
        container.add(radiusFieldDB, gc);
        smoothDonorBeforeButton = new JButton("Blur");
        smoothDonorBeforeButton.addActionListener(this);
        smoothDonorBeforeButton.setActionCommand("smoothDBefore");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 7;
        container.add(smoothDonorBeforeButton, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 8;
        container.add(new JLabel("Step 3b (optional): blur donor after image (Gaussian), radius in pixels:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 8;
        radiusFieldDA = new JTextField("2", 4);
        radiusFieldDA.setHorizontalAlignment(JTextField.RIGHT);
        container.add(radiusFieldDA, gc);
        smoothDonorAfterButton = new JButton("Blur");
        smoothDonorAfterButton.addActionListener(this);
        smoothDonorAfterButton.setActionCommand("smoothDAfter");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 8;
        container.add(smoothDonorAfterButton, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 9;
        container.add(new JLabel("Step 3c (optional): blur acceptor before image (Gaussian), radius in pixels:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 9;
        radiusFieldAB = new JTextField("2", 4);
        radiusFieldAB.setHorizontalAlignment(JTextField.RIGHT);
        container.add(radiusFieldAB, gc);
        smoothAcceptorBeforeButton = new JButton("Blur");
        smoothAcceptorBeforeButton.addActionListener(this);
        smoothAcceptorBeforeButton.setActionCommand("smoothABefore");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 9;
        container.add(smoothAcceptorBeforeButton, gc);

        gc.gridwidth = 9;
        gc.gridx = 0;
        gc.gridy = 10;
        container.add(new JLabel("Step 3d (optional): blur acceptor after image (Gaussian), radius in pixels:"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 10;
        radiusFieldAA = new JTextField("2", 4);
        radiusFieldAA.setHorizontalAlignment(JTextField.RIGHT);
        container.add(radiusFieldAA, gc);
        smoothAcceptorAfterButton = new JButton("Blur");
        smoothAcceptorAfterButton.addActionListener(this);
        smoothAcceptorAfterButton.setActionCommand("smoothAAfter");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 10;
        container.add(smoothAcceptorAfterButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 11;
        JPanel line3 = new JPanel();
        line3.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line3.setBackground(Color.lightGray);
        container.add(line3, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 12;
        container.add(new JLabel("Step 4a: subtract average of a background ROI of donor before image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 12;
        gc.insets = new Insets(0, 0, 0, 2);
        copyRoiButton = new JButton("Copy ROI");
        copyRoiButton.setToolTipText("Sets the same ROI for the donor after and acceptor images.");
        copyRoiButton.setMargin(new Insets(0, 0, 0, 0));
        copyRoiButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        copyRoiButton.addActionListener(this);
        copyRoiButton.setActionCommand("copyRoi");
        container.add(copyRoiButton, gc);

        subtractDonorBeforeButton = new JButton("Subtract");
        subtractDonorBeforeButton.addActionListener(this);
        subtractDonorBeforeButton.setActionCommand("subtractDonorBefore");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridx = 10;
        gc.gridy = 12;
        container.add(subtractDonorBeforeButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 13;
        container.add(new JLabel("Step 4b: subtract average of a background ROI of donor after image"), gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        subtractDonorAfterButton = new JButton("Subtract");
        subtractDonorAfterButton.addActionListener(this);
        subtractDonorAfterButton.setActionCommand("subtractDonorAfter");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 13;
        gc.insets = new Insets(2, 2, 2, 2);
        container.add(subtractDonorAfterButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 14;
        container.add(new JLabel("Step 4c (optional): subtract average of a background ROI of acceptor before image"), gc);
        subtractAcceptorBeforeButton = new JButton("Subtract");
        subtractAcceptorBeforeButton.addActionListener(this);
        subtractAcceptorBeforeButton.setActionCommand("subtractAcceptorBefore");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 14;
        container.add(subtractAcceptorBeforeButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 15;
        container.add(new JLabel("Step 4d (optional): subtract average of a background ROI of acceptor after image"), gc);
        subtractAcceptorAfterButton = new JButton("Subtract");
        subtractAcceptorAfterButton.addActionListener(this);
        subtractAcceptorAfterButton.setActionCommand("subtractAcceptorAfter");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 15;
        container.add(subtractAcceptorAfterButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 16;
        JPanel line4 = new JPanel();
        line4.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line4.setBackground(Color.lightGray);
        container.add(line4, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 17;
        container.add(new JLabel("Step 5a: set threshold for donor before image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 17;
        gc.insets = new Insets(0, 0, 0, 2);
        resetDBButton = new JButton("Reset");
        resetDBButton.setToolTipText("Resets blur and threshold settings");
        resetDBButton.setMargin(new Insets(0, 0, 0, 0));
        resetDBButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetDBButton.addActionListener(this);
        resetDBButton.setActionCommand("resetDB");
        container.add(resetDBButton, gc);
        thresholdDonorBeforeButton = new JButton("Set threshold");
        thresholdDonorBeforeButton.addActionListener(this);
        thresholdDonorBeforeButton.setActionCommand("thresholdDonorBefore");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 17;
        gc.insets = new Insets(2, 2, 2, 2);
        container.add(thresholdDonorBeforeButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 18;
        container.add(new JLabel("Step 5b: set threshold for donor after image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 18;
        gc.insets = new Insets(0, 0, 0, 2);
        resetDAButton = new JButton("Reset");
        resetDAButton.setToolTipText("Resets blur and threshold settings");
        resetDAButton.setMargin(new Insets(0, 0, 0, 0));
        resetDAButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetDAButton.addActionListener(this);
        resetDAButton.setActionCommand("resetDA");
        container.add(resetDAButton, gc);
        thresholdDonorAfterButton = new JButton("Set threshold");
        thresholdDonorAfterButton.addActionListener(this);
        thresholdDonorAfterButton.setActionCommand("thresholdDonorAfter");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 18;
        gc.insets = new Insets(2, 2, 2, 2);
        container.add(thresholdDonorAfterButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 19;
        container.add(new JLabel("Step 5c (optional): set threshold for acceptor before image"), gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 19;
        gc.insets = new Insets(0, 0, 0, 2);
        resetABButton = new JButton("Reset");
        resetABButton.setToolTipText("Resets blur and threshold settings");
        resetABButton.setMargin(new Insets(0, 0, 0, 0));
        resetABButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetABButton.addActionListener(this);
        resetABButton.setActionCommand("resetAB");
        container.add(resetABButton, gc);
        thresholdAcceptorBeforeButton = new JButton("Set threshold");
        thresholdAcceptorBeforeButton.addActionListener(this);
        thresholdAcceptorBeforeButton.setActionCommand("thresholdAcceptorBefore");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 19;
        gc.insets = new Insets(2, 2, 2, 2);
        container.add(thresholdAcceptorBeforeButton, gc);

        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 20;
        container.add(new JLabel("Step 5d (optional): set threshold for acceptor after image"), gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 21;
        gc.insets = new Insets(0, 6, 0, 6);
        JLabel thInfo = new JLabel("Threshold setting: set threshold, click Apply, then click Set to NaN");
        thInfo.setFont(new Font("Helvetica", Font.PLAIN, 10));
        container.add(thInfo, gc);
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 20;
        gc.insets = new Insets(0, 0, 0, 2);
        resetAAButton = new JButton("Reset");
        resetAAButton.setToolTipText("Resets blur and threshold settings");
        resetAAButton.setMargin(new Insets(0, 0, 0, 0));
        resetAAButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        resetAAButton.addActionListener(this);
        resetAAButton.setActionCommand("resetAA");
        container.add(resetAAButton, gc);
        thresholdAcceptorAfterButton = new JButton("Set threshold");
        thresholdAcceptorAfterButton.addActionListener(this);
        thresholdAcceptorAfterButton.setActionCommand("thresholdAcceptorAfter");
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 20;
        gc.insets = new Insets(2, 2, 2, 2);
        container.add(thresholdAcceptorAfterButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 22;
        JPanel line5 = new JPanel();
        line5.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line5.setBackground(Color.lightGray);
        container.add(line5, gc);

        // donor bleaching correction
        gc.gridwidth = 8;
        gc.gridx = 0;
        gc.gridy = 23;
        donorBlCorrLabel = new JLabel("Correction 1: calculate/set donor bleaching correction factor:");
        container.add(donorBlCorrLabel, gc);
        if (!donorBlCorrMenuItem.isSelected()) {
            donorBlCorrLabel.setVisible(false);
        }
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 23;
        donorBlCorrField = new JTextField("1.01", 4);
        donorBlCorrField.setHorizontalAlignment(JTextField.RIGHT);
        container.add(donorBlCorrField, gc);
        if (!donorBlCorrMenuItem.isSelected()) {
            donorBlCorrField.setVisible(false);
        }
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 23;
        gc.insets = new Insets(2, 2, 2, 2);
        calculateDBCorrButton = new JButton("Calculate");
        calculateDBCorrButton.addActionListener(this);
        calculateDBCorrButton.setActionCommand("calculateDonorBlCorrection");
        container.add(calculateDBCorrButton, gc);
        if (!donorBlCorrMenuItem.isSelected()) {
            calculateDBCorrButton.setVisible(false);
        }

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 24;
        lineDonorBlCorr = new JPanel();
        lineDonorBlCorr.setPreferredSize(new Dimension(windowWidth - 35, 1));
        lineDonorBlCorr.setBackground(Color.lightGray);
        container.add(lineDonorBlCorr, gc);
        if (!donorBlCorrMenuItem.isSelected()) {
            lineDonorBlCorr.setVisible(false);
        }

        // acceptor cross-talk correction
        gc.gridwidth = 8;
        gc.gridx = 0;
        gc.gridy = 25;
        accCrossTalkCorrLabel = new JLabel("Correction 2: calculate/set acceptor cross-talk correction factor:");
        container.add(accCrossTalkCorrLabel, gc);
        if (!accCrossTalkCorrMenuItem.isSelected()) {
            accCrossTalkCorrLabel.setVisible(false);
        }
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 25;
        accCrossTalkCorrField = new JTextField("0.01", 4);
        accCrossTalkCorrField.setHorizontalAlignment(JTextField.RIGHT);
        container.add(accCrossTalkCorrField, gc);
        if (!accCrossTalkCorrMenuItem.isSelected()) {
            accCrossTalkCorrField.setVisible(false);
        }
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 25;
        gc.insets = new Insets(2, 2, 2, 2);
        calculateAccCTCorrButton = new JButton("Calculate");
        calculateAccCTCorrButton.addActionListener(this);
        calculateAccCTCorrButton.setActionCommand("calculateAccCTCorrection");
        container.add(calculateAccCTCorrButton, gc);
        if (!accCrossTalkCorrMenuItem.isSelected()) {
            calculateAccCTCorrButton.setVisible(false);
        }

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 26;
        lineAccCrossTalk = new JPanel();
        lineAccCrossTalk.setPreferredSize(new Dimension(windowWidth - 35, 1));
        lineAccCrossTalk.setBackground(Color.lightGray);
        container.add(lineAccCrossTalk, gc);
        if (!accCrossTalkCorrMenuItem.isSelected()) {
            lineAccCrossTalk.setVisible(false);
        }

        // acceptor photoproduct correction
        gc.gridwidth = 8;
        gc.gridx = 0;
        gc.gridy = 27;
        accPhotoprCorrLabel = new JLabel("Correction 3: calculate/set acceptor photoproduct correction factor:");
        container.add(accPhotoprCorrLabel, gc);
        if (!accPhotoprCorrMenuItem.isSelected()) {
            accPhotoprCorrLabel.setVisible(false);
        }
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 27;
        accPhotoprCorrField = new JTextField("0.01", 4);
        accPhotoprCorrField.setHorizontalAlignment(JTextField.RIGHT);
        container.add(accPhotoprCorrField, gc);
        if (!accPhotoprCorrMenuItem.isSelected()) {
            accPhotoprCorrField.setVisible(false);
        }
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 27;
        gc.insets = new Insets(2, 2, 2, 2);
        calculateAccPPCorrButton = new JButton("Calculate");
        calculateAccPPCorrButton.addActionListener(this);
        calculateAccPPCorrButton.setActionCommand("calculateAccPPCorrection");
        container.add(calculateAccPPCorrButton, gc);
        if (!accPhotoprCorrMenuItem.isSelected()) {
            calculateAccPPCorrButton.setVisible(false);
        }

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 28;
        lineAccPhotopr = new JPanel();
        lineAccPhotopr.setPreferredSize(new Dimension(windowWidth - 35, 1));
        lineAccPhotopr.setBackground(Color.lightGray);
        container.add(lineAccPhotopr, gc);
        if (!accPhotoprCorrMenuItem.isSelected()) {
            lineAccPhotopr.setVisible(false);
        }

        // correction for partially bleached acceptor
        gc.gridwidth = 8;
        gc.gridx = 0;
        gc.gridy = 29;
        partialBlCorrLabel = new JLabel("Correction 4: calculate partial acceptor photobleaching correction factor:");
        container.add(partialBlCorrLabel, gc);
        if (!partialBlCorrMenuItem.isSelected()) {
            partialBlCorrLabel.setVisible(false);
        }
        gc.gridwidth = 1;
        gc.gridx = 9;
        gc.gridy = 29;
        partialBlCorrField = new JTextField("0", 4);
        partialBlCorrField.setHorizontalAlignment(JTextField.RIGHT);
        container.add(partialBlCorrField, gc);
        if (!partialBlCorrMenuItem.isSelected()) {
            partialBlCorrField.setVisible(false);
        }
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 29;
        gc.insets = new Insets(2, 2, 2, 2);
        calculatePartialBlCorrButton = new JButton("Calculate");
        calculatePartialBlCorrButton.addActionListener(this);
        calculatePartialBlCorrButton.setActionCommand("calculatePartialBlCorrection");
        container.add(calculatePartialBlCorrButton, gc);
        if (!partialBlCorrMenuItem.isSelected()) {
            calculatePartialBlCorrButton.setVisible(false);
        }

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 30;
        linePartialBl = new JPanel();
        linePartialBl.setPreferredSize(new Dimension(windowWidth - 35, 1));
        linePartialBl.setBackground(Color.lightGray);
        container.add(linePartialBl, gc);
        if (!partialBlCorrMenuItem.isSelected()) {
            linePartialBl.setVisible(false);
        }

        // create fret image
        JPanel createFretImgPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        gc.gridwidth = 10;
        gc.gridx = 0;
        gc.gridy = 31;
        gc.insets = new Insets(2, 2, 2, 2);
        gc.fill = GridBagConstraints.NONE;
        createFretImgPanel.add(new JLabel("Step 6: create FRET image  "));
        useAcceptorAsMask = new JCheckBox("use also acceptor before image as mask", true);
        useAcceptorAsMask.setToolTipText("<html>When calculating intramolecular FRET or intermolecular <br>FRET for one species, the AND of donor before and after <br>images is used as the default mask. If the acceptor label is <br>on another molecular species, the thresholded acceptor <br>image can be AND-ed to this as well.</html>");
        useAcceptorAsMask.setSelected(false);
        createFretImgPanel.add(useAcceptorAsMask);
        container.add(createFretImgPanel, gc);
        createButton = new JButton("Create");
        createButton.addActionListener(this);
        createButton.setActionCommand("createFretImage");
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 10;
        gc.gridy = 31;
        container.add(createButton, gc);

        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.gridx = 0;
        gc.gridy = 32;
        JPanel line7 = new JPanel();
        line7.setPreferredSize(new Dimension(windowWidth - 35, 1));
        line7.setBackground(Color.lightGray);
        container.add(line7, gc);

        gc.gridwidth = 8;
        gc.gridx = 0;
        gc.gridy = 33;
        container.add(new JLabel("Step 7: select ROIs and make measurements"), gc);
        gc.gridx = 9;
        gc.gridwidth = 1;
        closeImagesButton = new JButton("Close images");
        closeImagesButton.setToolTipText("Closes the source and transfer images and resets button colors");
        closeImagesButton.setMargin(new Insets(0, 0, 0, 0));
        closeImagesButton.setFont(new Font("Helvetica", Font.BOLD, 10));
        closeImagesButton.addActionListener(this);
        closeImagesButton.setActionCommand("closeImages");
        container.add(closeImagesButton, gc);
        measureButton = new JButton("Measure");
        measureButton.setMargin(new Insets(2, 2, 2, 2));
        measureButton.addActionListener(this);
        measureButton.setActionCommand("measureFretImage");
        gc.gridx = 10;
        gc.gridy = 33;
        container.add(measureButton, gc);
        nextButton = new JButton("Next");
        nextButton.setMargin(new Insets(2, 2, 2, 2));
        nextButton.addActionListener(this);
        nextButton.setActionCommand("nextImage");
        gc.gridx = 11;
        gc.gridy = 33;
        container.add(nextButton, gc);
        nextButton.setVisible(false);

        gc.weighty = 20;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.fill = GridBagConstraints.BOTH;
        gc.gridx = 0;
        gc.gridy = GridBagConstraints.RELATIVE;
        log = new JTextPane();
        log.setEditable(false);
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style style;
        style = log.addStyle("RED", defaultStyle);
        StyleConstants.setForeground(style, Color.red.darker());
        style = log.addStyle("BLUE", defaultStyle);
        StyleConstants.setForeground(style, Color.blue.darker());
        style = log.addStyle("BLACK", defaultStyle);
        StyleConstants.setForeground(style, Color.black.darker());
        logScrollPane = new JScrollPane(log);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Messages"));
        container.add(logScrollPane, gc);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getActionCommand().equals("exit")) {
                exit();
            } else if (e.getActionCommand().equals("split")) {
                if (WindowManager.getCurrentImage() == null) {
                    logError("No open image.");
                    return;
                }
                if (WindowManager.getCurrentImage().isHyperStack()) {
                    ij.plugin.HyperStackConverter hc = new ij.plugin.HyperStackConverter();
                    hc.run("hstostack");
                }
                StackEditor se = new StackEditor();
                se.run("toimages");
            } else if (e.getActionCommand().equals("applyMask")) {
                if (applyMaskDialog != null) {
                    applyMaskDialog.setVisible(false);
                    applyMaskDialog.dispose();
                }
                applyMaskDialog = new ApplyMaskDialog(this);
                applyMaskDialog.setVisible(true);
            } else if (e.getActionCommand().equals("bleachingMask")) {
                if (acceptorBefore == null) {
                    logError("No image is set as acceptor before bleaching.");
                    return;
                } else if (acceptorAfter == null) {
                    logError("No image is set as acceptor after bleaching.");
                    return;
                } else {
                    ImageProcessor ip1 = acceptorBefore.getProcessor();
                    ImageProcessor ip2 = acceptorAfter.getProcessor();
                    float[] ip1P = (float[]) ip1.getPixels();
                    float[] ip2P = (float[]) ip2.getPixels();

                    int width = ip1.getWidth();
                    int height = ip1.getHeight();
                    float[][] newImgPoints = new float[width][height];
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            newImgPoints[i][j] = ip1P[width * j + i] - ip2P[width * j + i];
                        }
                    }
                    FloatProcessor fp = new FloatProcessor(newImgPoints);
                    ImagePlus newImg = new ImagePlus("Acceptor before - acceptor after", fp);
                    newImg.changes = false;
                    newImg.show();
                    IJ.run("Threshold...");
                }
            } else if (e.getActionCommand().equals("calculateRatio")) {
                if (calculateImgRatioDialog != null) {
                    calculateImgRatioDialog.setVisible(false);
                    calculateImgRatioDialog.dispose();
                }
                calculateImgRatioDialog = new CalculateImgRatioDialog(this);
                calculateImgRatioDialog.setVisible(true);
            } else if (e.getActionCommand().equals("threshold")) {
                if (WindowManager.getCurrentImage() == null) {
                    logError("No open image.");
                    return;
                }
                IJ.run("Threshold...");
            } else if (e.getActionCommand().equals("convertto32bit")) {
                if (WindowManager.getCurrentImage() == null) {
                    logError("No open image.");
                    return;
                }
                new ImageConverter(WindowManager.getCurrentImage()).convertToGray32();
            } else if (e.getActionCommand().equals("shiftimage")) {
                if (WindowManager.getCurrentImage() == null) {
                    logError("No open image.");
                    return;
                }
                if (shiftDialog == null) {
                    shiftDialog = new ShiftDialog(this);
                }
                shiftDialog.setVisible(true);
            } else if (e.getActionCommand().equals("lutFire")) {
                if (WindowManager.getCurrentImage() == null) {
                    logError("No open image.");
                    return;
                }
                IJ.run("Fire");
            } else if (e.getActionCommand().equals("lutSpectrum")) {
                if (WindowManager.getCurrentImage() == null) {
                    logError("No open image.");
                    return;
                }
                IJ.run("Spectrum");
            } else if (e.getActionCommand().equals("histogram")) {
                if (WindowManager.getCurrentImage() == null) {
                    logError("No open image.");
                    return;
                }
                IJ.run("Histogram");
            } else if (e.getActionCommand().equals("openImage")) {
                (new Opener()).open();
            } else if (e.getActionCommand().equals("saveImageAsTiff")) {
                ImagePlus image = WindowManager.getCurrentImage();
                if (image == null) {
                    logError("No open image.");
                    return;
                }
                FileSaver fs = new FileSaver(image);
                if (fs.saveAsTiff()) {
                    log("Tiff file is saved.");
                }
                image.updateAndDraw();
            } else if (e.getActionCommand().equals("saveImageAsBmp")) {
                ImagePlus image = WindowManager.getCurrentImage();
                if (image == null) {
                    logError("No open image.");
                    return;
                }
                FileSaver fs = new FileSaver(image);
                if (fs.saveAsBmp()) {
                    log("Bmp file is saved.");
                }
                image.updateAndDraw();
            } else if (e.getActionCommand().equals("saveMessages")) {
                JFileChooser jfc = new JFileChooser(currentDirectory);
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setDialogTitle("Save messages...");
                jfc.showSaveDialog(this);
                if (jfc.getSelectedFile() == null) {
                    return;
                }
                if (jfc.getSelectedFile().exists()) {
                    currentDirectory = jfc.getCurrentDirectory().toString();
                    int resp = JOptionPane.showConfirmDialog(this,
                            "Overwrite existing file?", "Confirmation",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (resp == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                }
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(jfc.getSelectedFile().getAbsolutePath()));
                    out.write(log.getText());
                    out.close();
                } catch (IOException ioe) {
                    logError("Could not save messages.");
                }
            } else if (e.getActionCommand().equals("clearMessages")) {
                log.setText("");
            } else if (e.getActionCommand().equals("openLsmImage")) {
                JFileChooser jfc = new JFileChooser(currentDirectory);
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setDialogTitle("Open LSM image...");
                jfc.showOpenDialog(this);
                if (jfc.getSelectedFile() == null) {
                    return;
                }
                if (!jfc.getSelectedFile().exists()) {
                    logError("Selected file does not exist.");
                    return;
                }
                try {
                    currentDirectory = jfc.getCurrentDirectory().toString();
                    boolean close = false;
                    boolean resultsWindow = false;
                    while (WindowManager.getCurrentImage() != null) {
                        WindowManager.getCurrentImage().close();
                    }

                    resetAllButtonColors();

                    File imageFile = jfc.getSelectedFile();
                    (new Opener()).open(imageFile.getAbsolutePath());
                    WindowManager.putBehind();
                    this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "split"));
                    this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setAcceptorAfter"));
                    this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "lutSpectrum"));
                    WindowManager.putBehind();
                    this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setDonorAfter"));
                    this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "lutSpectrum"));
                    WindowManager.putBehind();
                    this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setAcceptorBefore"));
                    this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "lutSpectrum"));
                    WindowManager.putBehind();
                    this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setDonorBefore"));
                    this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "lutSpectrum"));
                } catch (Exception ex) {
                    logError("Could not open and set the selected LSM image.");
                    logException(ex.getMessage(), ex);
                }
            } else if (e.getActionCommand().equals("setDonorBefore")) {
                ImagePlus ip = WindowManager.getCurrentImage();
                if (ip == null) {
                    logError("No image is selected.");
                    setDonorBeforeButton.setBackground(originalButtonColor);
                    return;
                }
                if (ip.getNChannels() > 1) {
                    logError("Current image contains more than 1 channel (" + ip.getNChannels() + "). Please use: Image menu -> Split image.");
                    donorBefore = null;
                    setDonorBeforeButton.setBackground(originalButtonColor);
                    return;
                } else if (ip.getNSlices() > 1) {
                    logError("Current image contains more than 1 slice (" + ip.getNSlices() + "). Please use: Image menu -> Split image.");
                    donorBefore = null;
                    setDonorBeforeButton.setBackground(originalButtonColor);
                    return;
                }
                if (ip != null && donorAfter != null && ip.equals(donorAfter)) {
                    logError("The two donor images must not be the same. Please select and set an other image.");
                    donorBefore = null;
                    setDonorBeforeButton.setBackground(originalButtonColor);
                    return;
                }
                donorBefore = ip;
                donorBefore.setTitle("Donor before bleaching - " + new Date().toString());
                new ImageConverter(donorBefore).convertToGray32();
                if (automaticallyProcessedFiles == null) {
                    currentlyProcessedFileName = null;
                }
                setDonorBeforeButton.setBackground(greenColor);
            } else if (e.getActionCommand().equals("setDonorAfter")) {
                ImagePlus ip = WindowManager.getCurrentImage();
                if (ip == null) {
                    logError("No image is selected.");
                    setDonorAfterButton.setBackground(originalButtonColor);
                    return;
                }
                if (ip.getNChannels() > 1) {
                    logError("Current image contains more than 1 channel (" + ip.getNChannels() + "). Please split it into parts.");
                    donorAfter = null;
                    setDonorAfterButton.setBackground(originalButtonColor);
                    return;
                } else if (ip.getNSlices() > 1) {
                    logError("Current image contains more than 1 slice (" + ip.getNSlices() + "). Please split it into parts.");
                    donorAfter = null;
                    setDonorAfterButton.setBackground(originalButtonColor);
                    return;
                }
                if (donorBefore != null && ip != null && donorBefore.equals(ip)) {
                    logError("The two donor images must not be the same. Please select and set an other image.");
                    donorAfter = null;
                    setDonorAfterButton.setBackground(originalButtonColor);
                    return;
                }
                donorAfter = ip;
                donorAfter.setTitle("Donor after bleaching - " + new Date().toString());
                new ImageConverter(donorAfter).convertToGray32();
                setDonorAfterButton.setBackground(greenColor);
            } else if (e.getActionCommand().equals("setAcceptorBefore")) {
                acceptorBefore = WindowManager.getCurrentImage();
                if (acceptorBefore == null) {
                    logError("No image is selected.");
                    setAcceptorBeforeButton.setBackground(originalButtonColor);
                    return;
                }
                if (acceptorBefore.getNChannels() > 1) {
                    logError("Current image contains more than 1 channel (" + acceptorBefore.getNChannels() + "). Please split it into parts.");
                    acceptorBefore = null;
                    setAcceptorBeforeButton.setBackground(originalButtonColor);
                    return;
                } else if (acceptorBefore.getNSlices() > 1) {
                    logError("Current image contains more than 1 slice (" + acceptorBefore.getNSlices() + "). Please split it into parts.");
                    acceptorBefore = null;
                    setAcceptorBeforeButton.setBackground(originalButtonColor);
                    return;
                }
                acceptorBefore.setTitle("Acceptor before bleaching - " + new Date().toString());
                new ImageConverter(acceptorBefore).convertToGray32();
                acceptorBeforeSave = acceptorBefore.getProcessor().duplicate();
                setAcceptorBeforeButton.setBackground(greenColor);
            } else if (e.getActionCommand().equals("setAcceptorAfter")) {
                acceptorAfter = WindowManager.getCurrentImage();
                if (acceptorAfter == null) {
                    logError("No image is selected.");
                    setAcceptorAfterButton.setBackground(originalButtonColor);
                    return;
                }
                if (acceptorAfter.getNChannels() > 1) {
                    logError("Current image contains more than 1 channel (" + acceptorAfter.getNChannels() + "). Please split it into parts.");
                    acceptorAfter = null;
                    setAcceptorAfterButton.setBackground(originalButtonColor);
                    return;
                } else if (acceptorAfter.getNSlices() > 1) {
                    logError("Current image contains more than 1 slice (" + acceptorAfter.getNSlices() + "). Please split it into parts.");
                    acceptorAfter = null;
                    setAcceptorAfterButton.setBackground(originalButtonColor);
                    return;
                }
                acceptorAfter.setTitle("Acceptor after bleaching - " + new Date().toString());
                new ImageConverter(acceptorAfter).convertToGray32();
                acceptorAfterSave = acceptorAfter.getProcessor().duplicate();
                setAcceptorAfterButton.setBackground(greenColor);
            } else if (e.getActionCommand().equals("clearAB")) {
                if (acceptorBefore == null) {
                    logError("No image is set as acceptor before bleaching.");
                    return;
                }
                acceptorBefore = null;
                acceptorBeforeSave = null;
                setAcceptorBeforeButton.setBackground(originalButtonColor);
            } else if (e.getActionCommand().equals("clearAA")) {
                if (acceptorAfter == null) {
                    logError("No image is set as acceptor after bleaching.");
                    return;
                }
                acceptorAfter = null;
                acceptorAfterSave = null;
                setAcceptorAfterButton.setBackground(originalButtonColor);
            } else if (e.getActionCommand().equals("copyRoi")) {
                if (donorBefore == null) {
                    logError("No image is set as donor before bleaching.");
                    return;
                }
                if (donorBefore.getRoi() != null) {
                    if (donorAfter != null) {
                        donorAfter.setRoi(donorBefore.getRoi());
                    }
                    if (acceptorBefore != null) {
                        acceptorBefore.setRoi(donorBefore.getRoi());
                    }
                    if (acceptorAfter != null) {
                        acceptorAfter.setRoi(donorBefore.getRoi());
                    }
                } else {
                    if (donorAfter != null) {
                        donorAfter.killRoi();
                    }
                    if (acceptorBefore != null) {
                        acceptorBefore.killRoi();
                    }
                    if (acceptorAfter != null) {
                        acceptorAfter.killRoi();
                    }
                }
            } else if (e.getActionCommand().equals("subtractDonorBefore")) {
                if (donorBefore == null) {
                    logError("No image is set as donor before bleaching.");
                    return;
                } else if (donorBefore.getRoi() == null) {
                    logError("No ROI is defined for donor before bleaching.");
                    return;
                }

                int width = donorBefore.getWidth();
                int height = donorBefore.getHeight();
                double sum = 0;
                int count = 0;
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (donorBefore.getRoi().contains(i, j)) {
                            sum += donorBefore.getProcessor().getPixelValue(i, j);
                            count++;
                        }
                    }
                }
                float backgroundAvg = (float) (sum / count);

                float i = 0;
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        i = donorBefore.getProcessor().getPixelValue(x, y);
                        i = i - backgroundAvg;
                        if (i < 0) {
                            i = 0;
                        }
                        donorBefore.getProcessor().putPixelValue(x, y, i);
                    }
                }
                donorBefore.updateAndDraw();
                donorBefore.killRoi();
                donorBeforeSave = donorBefore.getProcessor().duplicate();
                log("Subtracted background (" + backgroundAvg + ") of donor before bleaching.");
                subtractDonorBeforeButton.setBackground(greenColor);
            } else if (e.getActionCommand().equals("subtractDonorAfter")) {
                if (donorAfter == null) {
                    logError("No image is set as donor after bleaching.");
                    return;
                } else if (donorAfter.getRoi() == null) {
                    logError("No ROI is defined for donor after bleaching.");
                    return;
                }

                int width = donorAfter.getWidth();
                int height = donorAfter.getHeight();
                double sum = 0;
                int count = 0;
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (donorAfter.getRoi().contains(i, j)) {
                            sum += donorAfter.getProcessor().getPixelValue(i, j);
                            count++;
                        }
                    }
                }
                float backgroundAvg = (float) (sum / count);

                float i = 0;
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        i = donorAfter.getProcessor().getPixelValue(x, y);
                        i = i - backgroundAvg;
                        if (i < 0) {
                            i = 0;
                        }
                        donorAfter.getProcessor().putPixelValue(x, y, i);
                    }
                }
                donorAfter.updateAndDraw();
                donorAfter.killRoi();
                donorAfterSave = donorAfter.getProcessor().duplicate();
                log("Subtracted background (" + backgroundAvg + ") of donor after bleaching.");
                subtractDonorAfterButton.setBackground(greenColor);
            } else if (e.getActionCommand().equals("subtractAcceptorBefore")) {
                if (acceptorBefore == null) {
                    logError("No image is set as acceptor before bleaching.");
                    return;
                } else if (acceptorBefore.getRoi() == null) {
                    logError("No ROI is defined for acceptor before bleaching.");
                    return;
                }

                int width = acceptorBefore.getWidth();
                int height = acceptorBefore.getHeight();
                double sum = 0;
                int count = 0;
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (acceptorBefore.getRoi().contains(i, j)) {
                            sum += acceptorBefore.getProcessor().getPixelValue(i, j);
                            count++;
                        }
                    }
                }
                float backgroundAvg = (float) (sum / count);

                float i = 0;
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        i = acceptorBefore.getProcessor().getPixelValue(x, y) - backgroundAvg;
                        if (i < 0) {
                            i = 0;
                        }
                        acceptorBefore.getProcessor().putPixelValue(x, y, i);
                    }
                }
                acceptorBefore.updateAndDraw();
                acceptorBefore.killRoi();
                acceptorBeforeSave = acceptorBefore.getProcessor().duplicate();
                log("Subtracted background (" + backgroundAvg + ") of acceptor before bleaching.");
                subtractAcceptorBeforeButton.setBackground(greenColor);
            } else if (e.getActionCommand().equals("subtractAcceptorAfter")) {
                if (acceptorAfter == null) {
                    logError("No image is set as acceptor after bleaching.");
                    return;
                } else if (acceptorAfter.getRoi() == null) {
                    logError("No ROI is defined for acceptor after bleaching.");
                    return;
                }

                int width = acceptorAfter.getWidth();
                int height = acceptorAfter.getHeight();
                double sum = 0;
                int count = 0;
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (acceptorAfter.getRoi().contains(i, j)) {
                            sum += acceptorAfter.getProcessor().getPixelValue(i, j);
                            count++;
                        }
                    }
                }
                float backgroundAvg = (float) (sum / count);

                float i = 0;
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        i = acceptorAfter.getProcessor().getPixelValue(x, y) - backgroundAvg;
                        if (i < 0) {
                            i = 0;
                        }
                        acceptorAfter.getProcessor().putPixelValue(x, y, i);
                    }
                }
                acceptorAfter.updateAndDraw();
                acceptorAfter.killRoi();
                acceptorAfterSave = acceptorAfter.getProcessor().duplicate();
                log("Subtracted background (" + backgroundAvg + ") of acceptor after bleaching.");
                subtractAcceptorAfterButton.setBackground(greenColor);
            } else if (e.getActionCommand().equals("thresholdDonorBefore")) {
                if (donorBefore == null) {
                    logError("No image is set as donor before bleaching.");
                    return;
                }
                IJ.selectWindow(donorBefore.getTitle());
                IJ.run("Threshold...");
                thresholdDonorBeforeButton.setBackground(greenColor);
            } else if (e.getActionCommand().equals("thresholdDonorAfter")) {
                if (donorAfter == null) {
                    logError("No image is set as donor after bleaching.");
                    return;
                }
                IJ.selectWindow(donorAfter.getTitle());
                IJ.run("Threshold...");
                thresholdDonorAfterButton.setBackground(greenColor);
            } else if (e.getActionCommand().equals("thresholdAcceptorBefore")) {
                if (acceptorBefore == null) {
                    logError("No image is set as acceptor before bleaching.");
                    return;
                }
                IJ.selectWindow(acceptorBefore.getTitle());
                IJ.run("Threshold...");
                thresholdAcceptorBeforeButton.setBackground(greenColor);
            } else if (e.getActionCommand().equals("thresholdAcceptorAfter")) {
                if (acceptorAfter == null) {
                    logError("No image is set as acceptor after bleaching.");
                    return;
                }
                IJ.selectWindow(acceptorAfter.getTitle());
                IJ.run("Threshold...");
                thresholdAcceptorAfterButton.setBackground(greenColor);
            } else if (e.getActionCommand().equals("resetDB")) {
                if (donorBefore == null) {
                    logError("No image is set as donor before bleaching.");
                    return;
                }
                if (donorBeforeSave == null) {
                    logError("No saved image.");
                    return;
                }
                donorBefore.setProcessor(donorBefore.getTitle(), donorBeforeSave.duplicate());
                donorBefore.updateAndDraw();
                thresholdDonorBeforeButton.setBackground(originalButtonColor);
                smoothDonorBeforeButton.setBackground(originalButtonColor);
            } else if (e.getActionCommand().equals("resetDA")) {
                if (donorAfter == null) {
                    logError("No image is set as donor after bleaching.");
                    return;
                }
                if (donorAfterSave == null) {
                    logError("No saved image.");
                    return;
                }
                donorAfter.setProcessor(donorAfter.getTitle(), donorAfterSave.duplicate());
                donorAfter.updateAndDraw();
                thresholdDonorAfterButton.setBackground(originalButtonColor);
                smoothDonorAfterButton.setBackground(originalButtonColor);
            } else if (e.getActionCommand().equals("resetAB")) {
                if (acceptorBefore == null) {
                    logError("No image is set as acceptor before bleaching.");
                    return;
                }
                if (acceptorBeforeSave == null) {
                    logError("No saved image.");
                    return;
                }
                acceptorBefore.setProcessor(acceptorBefore.getTitle(), acceptorBeforeSave.duplicate());
                acceptorBefore.updateAndDraw();
                thresholdAcceptorBeforeButton.setBackground(originalButtonColor);
                smoothAcceptorBeforeButton.setBackground(originalButtonColor);
            } else if (e.getActionCommand().equals("resetAA")) {
                if (acceptorAfter == null) {
                    logError("No image is set as acceptor after bleaching.");
                    return;
                }
                if (acceptorAfterSave == null) {
                    logError("No saved image.");
                    return;
                }
                acceptorAfter.setProcessor(acceptorAfter.getTitle(), acceptorAfterSave.duplicate());
                acceptorAfter.updateAndDraw();
                thresholdAcceptorAfterButton.setBackground(originalButtonColor);
                smoothAcceptorAfterButton.setBackground(originalButtonColor);
            } else if (e.getActionCommand().equals("smoothDBefore")) {
                if (donorBefore == null) {
                    logError("No image is set as donor before bleaching.");
                    return;
                } else {
                    if (radiusFieldDB.getText().trim().equals("")) {
                        logError("Radius has to be given for Gaussian blur.");
                        return;
                    } else {
                        double radius = 0;
                        try {
                            radius = Double.parseDouble(radiusFieldDB.getText().trim());
                        } catch (Exception ex) {
                            logError("Radius has to be given for Gaussian blur.");
                            return;
                        }
                        GaussianBlur gb = new GaussianBlur();
                        if (!gb.blur(donorBefore.getProcessor(), radius)) {
                            return;
                        }
                        donorBefore.updateAndDraw();
                        smoothDonorBeforeButton.setBackground(greenColor);
                        log("Gaussian blurred donor before bleaching.");
                    }
                }
            } else if (e.getActionCommand().equals("smoothDAfter")) {
                if (donorAfter == null) {
                    logError("No image is set as donor after bleaching.");
                    return;
                } else {
                    if (radiusFieldDA.getText().trim().equals("")) {
                        logError("Radius has to be given for Gaussian blur.");
                        return;
                    } else {
                        double radius = 0;
                        try {
                            radius = Double.parseDouble(radiusFieldDA.getText().trim());
                        } catch (Exception ex) {
                            logError("Radius has to be given for Gaussian blur.");
                            return;
                        }
                        GaussianBlur gb = new GaussianBlur();
                        if (!gb.blur(donorAfter.getProcessor(), radius)) {
                            return;
                        }
                        donorAfter.updateAndDraw();
                        smoothDonorAfterButton.setBackground(greenColor);
                        log("Gaussian blurred donor after bleaching.");
                    }
                }
            } else if (e.getActionCommand().equals("smoothABefore")) {
                if (acceptorBefore == null) {
                    logError("No image is set as acceptor before bleaching.");
                    return;
                } else {
                    if (radiusFieldAB.getText().trim().equals("")) {
                        logError("Radius has to be given for Gaussian blur.");
                        return;
                    } else {
                        double radius = 0;
                        try {
                            radius = Double.parseDouble(radiusFieldAB.getText().trim());
                        } catch (Exception ex) {
                            logError("Radius has to be given for Gaussian blur.");
                            return;
                        }
                        GaussianBlur gb = new GaussianBlur();
                        if (!gb.blur(acceptorBefore.getProcessor(), radius)) {
                            return;
                        }
                        acceptorBefore.updateAndDraw();
                        smoothAcceptorBeforeButton.setBackground(greenColor);
                        log("Gaussian blurred acceptor before bleaching.");
                    }
                }
            } else if (e.getActionCommand().equals("smoothAAfter")) {
                if (acceptorAfter == null) {
                    logError("No image is set as acceptor after bleaching.");
                    return;
                } else {
                    if (radiusFieldAB.getText().trim().equals("")) {
                        logError("Radius has to be given for Gaussian blur.");
                        return;
                    } else {
                        double radius = 0;
                        try {
                            radius = Double.parseDouble(radiusFieldAB.getText().trim());
                        } catch (Exception ex) {
                            logError("Radius has to be given for Gaussian blur.");
                            return;
                        }
                        GaussianBlur gb = new GaussianBlur();
                        if (!gb.blur(acceptorAfter.getProcessor(), radius)) {
                            return;
                        }
                        acceptorAfter.updateAndDraw();
                        smoothAcceptorAfterButton.setBackground(greenColor);
                        log("Gaussian blurred acceptor after bleaching.");
                    }
                }
            } else if (e.getActionCommand().equals("registerImages")) {
                if (donorBefore == null) {
                    logError("No image is set as donor before bleaching.");
                    return;
                } else if (donorAfter == null) {
                    logError("No image is set as donor after bleaching.");
                    return;
                } else {
                    DecimalFormat df = new DecimalFormat("#0.000");
                    FHT fht1 = new FHT(donorBefore.getProcessor().duplicate());
                    fht1.transform();
                    FHT fht2 = new FHT(donorAfter.getProcessor().duplicate());
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
                    if (maxx != 0 || maxy != 0) {
                        ShiftDialog sd = new ShiftDialog(this);
                        if (maxy > height / 2) {
                            log("Shifting donor after image up " + (height - maxy) + " pixel" + ((height - maxy) > 1 ? "s" : "") + ".");
                            sd.shiftUp(donorAfter, height - maxy);
                            if (applyShiftCB.isSelected()) {
                                sd.shiftUp(acceptorAfter, height - maxy);
                            }
                        } else if (maxy != 0) {
                            log("Shifting donor after image down " + maxy + " pixel" + (maxy > 1 ? "s" : "") + ".");
                            sd.shiftDown(donorAfter, maxy);
                            if (applyShiftCB.isSelected()) {
                                sd.shiftDown(acceptorAfter, maxy);
                            }
                        }
                        if (maxx > width / 2) {
                            log("Shifting donor after image to the left " + (width - maxx) + " pixel" + ((width - maxx) > 1 ? "s" : "") + ".");
                            sd.shiftLeft(donorAfter, width - maxx);
                            if (applyShiftCB.isSelected()) {
                                sd.shiftLeft(acceptorAfter, width - maxx);
                            }
                        } else if (maxx != 0) {
                            log("Shifting donor after image to the right " + maxx + " pixel" + (maxx > 1 ? "s" : "") + ".");
                            sd.shiftRight(donorAfter, maxx);
                            if (applyShiftCB.isSelected()) {
                                sd.shiftRight(acceptorAfter, maxx);
                            }
                        }
                        actionPerformed(new ActionEvent(registerButton, 1, "registerImages"));
                    } else {
                        double countAll = 0;
                        double count = 0;
                        float db, da = 0;
                        double p = 1.10;
                        log("Registration finished.");
                        Roi roi = donorBefore.getRoi();
                        if (roi == null) {
                            logWarning("The calculated statistics after registration are more authoritative if there is a ROI defined in the donor before image, and the calculations are based on that.");
                            donorAfter.killRoi();
                        } else {
                            donorAfter.setRoi(donorBefore.getRoi());
                        }

                        ImageStatistics isMeanDB = ImageStatistics.getStatistics(donorBefore.getProcessor(), Measurements.MEAN, null);
                        ImageStatistics isMeanDA = ImageStatistics.getStatistics(donorAfter.getProcessor(), Measurements.MEAN, null);
                        ImageStatistics isStdDevDB = ImageStatistics.getStatistics(donorBefore.getProcessor(), Measurements.STD_DEV, null);
                        ImageStatistics isStdDevDA = ImageStatistics.getStatistics(donorAfter.getProcessor(), Measurements.STD_DEV, null);

                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                if (roi != null && roi.contains(x, y)) {
                                    countAll++;
                                    db = donorBefore.getProcessor().getPixelValue(x, y);
                                    da = donorAfter.getProcessor().getPixelValue(x, y);
                                    if (db != 0 && da != 0 && db / da > p) {
                                        count++;
                                    }
                                } else if (roi == null) {
                                    countAll++;
                                    db = donorBefore.getProcessor().getPixelValue(x, y);
                                    da = donorAfter.getProcessor().getPixelValue(x, y);
                                    if (db != 0 && da != 0 && db / da > p) {
                                        count++;
                                    }
                                }
                            }
                        }
                        log("Relative dispersion (SD/mean) of donor before bleaching image: " + df.format((float) (isStdDevDB.stdDev / isMeanDB.mean)));
                        log("Relative dispersion (SD/mean) of donor after bleaching image: " + df.format((float) (isStdDevDA.stdDev / isMeanDA.mean)));
                        df.applyPattern("#0.0");
                        log(df.format(count / countAll * 100) + "% of pixels has lower intensity by 10% in the donor after than in the donor before image.");
                        registerButton.setBackground(greenColor);
                    }
                }
            } else if (e.getActionCommand().equals("useLsmImages")) {
                if (useLsmImages.isSelected()) {
                    setDonorBeforeButton.setText("Open & Set LSM");
                    setDonorBeforeButton.setActionCommand("openLsmImage");
                    setDonorAfterButton.setEnabled(false);
                    setAcceptorBeforeButton.setEnabled(false);
                    setAcceptorAfterButton.setEnabled(false);
                } else {
                    setDonorBeforeButton.setText("Set image");
                    setDonorBeforeButton.setActionCommand("setDonorBefore");
                    setDonorAfterButton.setEnabled(true);
                    setAcceptorBeforeButton.setEnabled(true);
                    setAcceptorAfterButton.setEnabled(true);
                }
                logScrollPane.setPreferredSize(new Dimension(10, 10));
            } else if (e.getActionCommand().equals("donorblcorrm")) {
                if (donorBlCorrMenuItem.isSelected()) {
                    donorBlCorrLabel.setVisible(true);
                    donorBlCorrField.setVisible(true);
                    calculateDBCorrButton.setVisible(true);
                    lineDonorBlCorr.setVisible(true);
                    logScrollPane.setPreferredSize(new Dimension(10, 10));
                } else {
                    donorBlCorrLabel.setVisible(false);
                    donorBlCorrField.setVisible(false);
                    calculateDBCorrButton.setVisible(false);
                    lineDonorBlCorr.setVisible(false);
                    logScrollPane.setPreferredSize(new Dimension(10, 10));
                    if (donorBlCorrDialog != null) {
                        donorBlCorrDialog.setVisible(false);
                        donorBlCorrDialog.dispose();
                    }
                }
            } else if (e.getActionCommand().equals("acccrtalkcorrm")) {
                if (accCrossTalkCorrMenuItem.isSelected()) {
                    accCrossTalkCorrLabel.setVisible(true);
                    accCrossTalkCorrField.setVisible(true);
                    calculateAccCTCorrButton.setVisible(true);
                    lineAccCrossTalk.setVisible(true);
                    logScrollPane.setPreferredSize(new Dimension(10, 10));
                } else {
                    accCrossTalkCorrLabel.setVisible(false);
                    accCrossTalkCorrField.setVisible(false);
                    calculateAccCTCorrButton.setVisible(false);
                    lineAccCrossTalk.setVisible(false);
                    logScrollPane.setPreferredSize(new Dimension(10, 10));
                    if (acceptorCTCorrDialog != null) {
                        acceptorCTCorrDialog.setVisible(false);
                        acceptorCTCorrDialog.dispose();
                    }
                }
            } else if (e.getActionCommand().equals("accphprcorrm")) {
                if (accPhotoprCorrMenuItem.isSelected()) {
                    accPhotoprCorrLabel.setVisible(true);
                    accPhotoprCorrField.setVisible(true);
                    calculateAccPPCorrButton.setVisible(true);
                    lineAccPhotopr.setVisible(true);
                    logScrollPane.setPreferredSize(new Dimension(10, 10));
                } else {
                    accPhotoprCorrLabel.setVisible(false);
                    accPhotoprCorrField.setVisible(false);
                    calculateAccPPCorrButton.setVisible(false);
                    lineAccPhotopr.setVisible(false);
                    logScrollPane.setPreferredSize(new Dimension(10, 10));
                    if (acceptorPPCorrDialog != null) {
                        acceptorPPCorrDialog.setVisible(false);
                        acceptorPPCorrDialog.dispose();
                    }
                }
            } else if (e.getActionCommand().equals("partialblcorrm")) {
                if (partialBlCorrMenuItem.isSelected()) {
                    partialBlCorrLabel.setVisible(true);
                    partialBlCorrField.setVisible(true);
                    calculatePartialBlCorrButton.setVisible(true);
                    linePartialBl.setVisible(true);
                    logScrollPane.setPreferredSize(new Dimension(10, 10));
                } else {
                    partialBlCorrLabel.setVisible(false);
                    partialBlCorrField.setVisible(false);
                    calculatePartialBlCorrButton.setVisible(false);
                    linePartialBl.setVisible(false);
                    logScrollPane.setPreferredSize(new Dimension(10, 10));
                }
            } else if (e.getActionCommand().equals("calculateDonorBlCorrection")) {
                if (donorBlCorrDialog != null) {
                    donorBlCorrDialog.setVisible(false);
                    donorBlCorrDialog.dispose();
                }
                donorBlCorrDialog = new DonorBlCorrDialog(this);
                donorBlCorrDialog.setVisible(true);
            } else if (e.getActionCommand().equals("calculateAccCTCorrection")) {
                if (acceptorCTCorrDialog != null) {
                    acceptorCTCorrDialog.setVisible(false);
                    acceptorCTCorrDialog.dispose();
                }
                acceptorCTCorrDialog = new AcceptorCTCorrDialog(this);
                acceptorCTCorrDialog.setVisible(true);
            } else if (e.getActionCommand().equals("calculateAccPPCorrection")) {
                if (acceptorPPCorrDialog != null) {
                    acceptorPPCorrDialog.setVisible(false);
                    acceptorPPCorrDialog.dispose();
                }
                acceptorPPCorrDialog = new AcceptorPPCorrDialog(this);
                acceptorPPCorrDialog.setVisible(true);
            } else if (e.getActionCommand().equals("calculatePartialBlCorrection")) {
                if (acceptorBefore == null) {
                    logError("No image is set as acceptor before bleaching.");
                    return;
                } else if (acceptorAfter == null) {
                    logError("No image is set as acceptor after bleaching.");
                    return;
                } else {
                    ImageProcessor ipAB = acceptorBefore.getProcessor();
                    ImageProcessor ipAA = acceptorAfter.getProcessor();
                    int width = acceptorBefore.getWidth();
                    int height = acceptorBefore.getHeight();
                    double sum = 0;
                    int count = 0;
                    if (acceptorBefore.getRoi() != null) {
                        acceptorAfter.setRoi(acceptorBefore.getRoi());
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (acceptorBefore.getRoi().contains(i, j)) {
                                    if (!Float.isNaN(ipAA.getPixelValue(i, j)) && !Float.isNaN(ipAB.getPixelValue(i, j))) {
                                        sum += ipAA.getPixelValue(i, j) / ipAB.getPixelValue(i, j);
                                        count++;
                                    }
                                }
                            }
                        }
                    } else {
                        logWarning("No ROI is defined for acceptor before bleaching.");
                        acceptorAfter.killRoi();
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                if (!Float.isNaN(ipAA.getPixelValue(i, j)) && !Float.isNaN(ipAB.getPixelValue(i, j))) {
                                    sum += ipAA.getPixelValue(i, j) / ipAB.getPixelValue(i, j);
                                    count++;
                                }
                            }
                        }
                    }
                    float partialBlCorrFactor = (float) (sum / count);
                    DecimalFormat df = new DecimalFormat("#.###");
                    partialBlCorrField.setText(df.format(partialBlCorrFactor).toString());
                    calculatePartialBlCorrButton.setBackground(greenColor);
                }
            } else if (e.getActionCommand().equals("createFretImage")) {
                if (donorBefore == null) {
                    logError("No image is set as donor before bleaching.");
                    return;
                } else if (donorAfter == null) {
                    logError("No image is set as donor after bleaching.");
                    return;
                } else if ((useAcceptorAsMask.isSelected() || accCrossTalkCorrMenuItem.isSelected() || accPhotoprCorrMenuItem.isSelected()) && acceptorBefore == null) {
                    logError("No image is set as acceptor before bleaching.");
                    return;
                } else {
                    if (donorBlCorrMenuItem.isSelected() && donorBlCorrField.getText().trim().equals("")) {
                        logError("Bleaching correction factor has to be given.");
                        return;
                    } else if (accCrossTalkCorrMenuItem.isSelected() && accCrossTalkCorrField.getText().trim().equals("")) {
                        logError("Acceptor cross-talk correction factor has to be given.");
                        return;
                    } else if (accPhotoprCorrMenuItem.isSelected() && accPhotoprCorrField.getText().trim().equals("")) {
                        logError("Acceptor photoproduct correction factor has to be given.");
                        return;
                    } else if (partialBlCorrMenuItem.isSelected() && partialBlCorrField.getText().trim().equals("")) {
                        logError("Partial acceptor photobleaching correction factor has to be given.");
                        return;
                    } else {
                        float donorBlCorr = 1;
                        if (donorBlCorrMenuItem.isSelected()) {
                            try {
                                donorBlCorr = Float.parseFloat(donorBlCorrField.getText().trim());
                            } catch (Exception ex) {
                                logError("Donor bleaching correction factor has to be given.");
                                return;
                            }
                            if (donorBlCorr < 1) {
                                logWarning("The donor bleaching correction factor should not be lower than 1.");
                            }
                        }
                        float acceptorCTCorr = 0;
                        if (accCrossTalkCorrMenuItem.isSelected()) {
                            try {
                                acceptorCTCorr = Float.parseFloat(accCrossTalkCorrField.getText().trim());
                            } catch (Exception ex) {
                                logError("Acceptor cross-talk correction factor has to be given.");
                                return;
                            }
                            if (acceptorCTCorr < 0) {
                                logWarning("The acceptor cross-talk correction factor should not be lower than 0.");
                            }
                        }
                        float acceptorPPCorr = 0;
                        if (accPhotoprCorrMenuItem.isSelected()) {
                            try {
                                acceptorPPCorr = Float.parseFloat(accPhotoprCorrField.getText().trim());
                            } catch (Exception ex) {
                                logError("Acceptor photoproduct correction factor has to be given.");
                                return;
                            }
                            if (acceptorPPCorr < 0) {
                                logWarning("The acceptor photoproduct correction factor should not be lower than 0.");
                            }
                        }
                        float partialBlCorr = 0;
                        if (partialBlCorrMenuItem.isSelected()) {
                            try {
                                partialBlCorr = Float.parseFloat(partialBlCorrField.getText().trim());
                            } catch (Exception ex) {
                                logError("Partial acceptor photobleaching correction factor has to be given.");
                                return;
                            }
                            if (partialBlCorr < 0) {
                                logWarning("The partial acceptor photobleaching correction should not be lower than 0.");
                            }
                            if (partialBlCorr > 1) {
                                logWarning("The partial acceptor photobleaching correction should not be higher than 1.");
                            }
                        }
                        ImageProcessor ipDB = donorBefore.getProcessor().duplicate();
                        ImageProcessor ipDA = donorAfter.getProcessor().duplicate();
                        ImageProcessor ipAB = null;
                        if (acceptorBefore != null) {
                            ipAB = acceptorBefore.getProcessor().duplicate();
                        }

                        float[] ipDBP = (float[]) ipDB.getPixels();
                        float[] ipDAP = (float[]) ipDA.getPixels();
                        float[] ipABP = null;
                        if (ipAB != null) {
                            ipABP = (float[]) ipAB.getPixels();
                        }

                        if (!partialBlCorrMenuItem.isSelected()) {
                            // acceptor cross-talk correction
                            if (accCrossTalkCorrMenuItem.isSelected()) {
                                for (int i = 0; i < ipABP.length; i++) {
                                    if (!Float.isNaN(ipDBP[i]) && !Float.isNaN(ipABP[i])) {
                                        ipDBP[i] = ipDBP[i] - ipABP[i] * acceptorCTCorr;
                                    } else {
                                        ipDBP[i] = Float.NaN;
                                    }
                                }
                            }

                            // acceptor photoproduct correction
                            if (accPhotoprCorrMenuItem.isSelected()) {
                                for (int i = 0; i < ipABP.length; i++) {
                                    if (!Float.isNaN(ipDAP[i]) && !Float.isNaN(ipABP[i])) {
                                        ipDAP[i] = ipDAP[i] - ipABP[i] * acceptorPPCorr;
                                    } else {
                                        ipDAP[i] = Float.NaN;
                                    }
                                }
                            }

                            // donor bleaching correction
                            if (donorBlCorrMenuItem.isSelected()) {
                                for (int i = 0; i < ipDAP.length; i++) {
                                    if (!Float.isNaN(ipDAP[i])) {
                                        ipDAP[i] = ipDAP[i] * donorBlCorr;
                                    } else {
                                        ipDAP[i] = Float.NaN;
                                    }
                                }
                            }

                            for (int i = 0; i < ipDAP.length; i++) {
                                ipDAP[i] = (float) 1 - (ipDBP[i] / ipDAP[i]);
                            }
                        } else {
                            if (accCrossTalkCorrMenuItem.isSelected()) {
                                for (int i = 0; i < ipABP.length; i++) {
                                    if (!Float.isNaN(ipDBP[i]) && !Float.isNaN(ipABP[i])) {
                                        ipDBP[i] = ipDBP[i] - ipABP[i] * acceptorCTCorr;
                                    } else {
                                        ipDBP[i] = Float.NaN;
                                    }
                                }
                            }

                            for (int i = 0; i < ipDBP.length; i++) {
                                if (accCrossTalkCorrMenuItem.isSelected() || accPhotoprCorrMenuItem.isSelected()) {
                                    if (!Float.isNaN(ipDBP[i]) && !Float.isNaN(ipDAP[i]) && !Float.isNaN(ipABP[i])) {
                                        ipDAP[i] = (float) ((double) donorBlCorr * ((double) ipDAP[i] - ((double) partialBlCorr * (double) acceptorCTCorr + (double) acceptorPPCorr * ((double) 1 - (double) partialBlCorr)) * (double) ipABP[i]) - (double) partialBlCorr * (double) ipDBP[i]);
                                    } else {
                                        ipDAP[i] = Float.NaN;
                                    }
                                } else {
                                    if (!Float.isNaN(ipDBP[i]) && !Float.isNaN(ipDAP[i])) {
                                        ipDAP[i] = (float) ((double) donorBlCorr * (double) ipDAP[i] - (double) partialBlCorr * (double) ipDBP[i]);
                                    } else {
                                        ipDAP[i] = Float.NaN;
                                    }
                                }
                            }

                            for (int i = 0; i < ipDAP.length; i++) {
                                ipDAP[i] = (float) ((double) 1 - (((double) 1 - (double) partialBlCorr) * (double) ipDBP[i] / (double) ipDAP[i]));
                            }
                        }

                        int width = ipDA.getWidth();
                        int height = ipDA.getHeight();
                        float[][] tiPoints = new float[width][height];
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                tiPoints[i][j] = ipDAP[width * j + i];
                            }
                        }

                        float iv = 0;
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                iv = tiPoints[x][y];
                                if (useAcceptorAsMask.isSelected() && (Float.isNaN(ipABP[width * y + x]) || ipABP[width * y + x] == 0)) {
                                    tiPoints[x][y] = Float.NaN;
                                }
                            }
                        }

                        FloatProcessor tiFp = new FloatProcessor(tiPoints);
                        if (transferImage != null && transferImage.getProcessor() != null) {
                            ColorModel cm = transferImage.getProcessor().getColorModel();
                            transferImage.setProcessor("Transfer image", tiFp);
                            transferImage.getProcessor().setColorModel(cm);
                            transferImage.updateAndDraw();
                        } else {
                            transferImage = new ImagePlus("Transfer image", tiFp);
                            transferImage.show();
                        }

                        analyzer = new Analyzer();
                        resultsTable = Analyzer.getResultsTable();
                        resultsTable.setPrecision(3);
                        resultsTable.incrementCounter();
                        int widthTi = transferImage.getWidth();
                        int heightTi = transferImage.getHeight();
                        int currentRow = resultsTable.getCounter();
                        if (currentlyProcessedFileName != null) {
                            resultsTable.setValue("File", currentRow, currentlyProcessedFileName);
                        }
                        if (transferImage.getRoi() != null) {
                            Roi roi = transferImage.getRoi();
                            int count = 0;
                            int notNan = 0;
                            for (int i = 0; i < widthTi; i++) {
                                for (int j = 0; j < heightTi; j++) {
                                    if (roi.contains(i, j)) {
                                        count++;
                                        if (transferImage.getProcessor().getPixelValue(i, j) >= -1) {
                                            notNan++;
                                        }
                                    }
                                }
                            }
                            resultsTable.addValue("Pixels", count);
                            resultsTable.addValue("Not NaN p.", notNan);
                        } else {
                            int notNan = 0;
                            for (int i = 0; i < widthTi; i++) {
                                for (int j = 0; j < heightTi; j++) {
                                    if (transferImage.getProcessor().getPixelValue(i, j) >= -1) {
                                        notNan++;
                                    }
                                }
                            }
                            resultsTable.addValue("Pixels", widthTi * heightTi);
                            resultsTable.addValue("Not NaN p.", notNan);
                        }
                        ImageStatistics isMean = ImageStatistics.getStatistics(tiFp, Measurements.MEAN, null);
                        resultsTable.addValue("Mean", (float) isMean.mean);
                        ImageStatistics isMedian = ImageStatistics.getStatistics(tiFp, Measurements.MEDIAN, null);
                        resultsTable.addValue("Median", (float) isMedian.median);
                        ImageStatistics isStdDev = ImageStatistics.getStatistics(tiFp, Measurements.STD_DEV, null);
                        resultsTable.addValue("Std. dev.", (float) isStdDev.stdDev);
                        ImageStatistics isMinMax = ImageStatistics.getStatistics(tiFp, Measurements.MIN_MAX, null);
                        resultsTable.addValue("Min", (float) isMinMax.min);
                        resultsTable.addValue("Max", (float) isMinMax.max);
                        if (transferImage.getRoi() != null) {
                            donorBefore.setRoi(transferImage.getRoi());
                            donorAfter.setRoi(transferImage.getRoi());
                            if (acceptorBefore != null) {
                                acceptorBefore.setRoi(transferImage.getRoi());
                            }
                        } else {
                            donorBefore.killRoi();
                            donorAfter.killRoi();
                            if (acceptorBefore != null) {
                                acceptorBefore.killRoi();
                            }
                        }
                        ImageStatistics isMinMaxDB = ImageStatistics.getStatistics(donorBefore.getProcessor(), Measurements.MIN_MAX, null);
                        resultsTable.addValue("Min (DB)", (float) isMinMaxDB.min);
                        resultsTable.addValue("Max (DB)", (float) isMinMaxDB.max);
                        ImageStatistics isDBMean = ImageStatistics.getStatistics(donorBefore.getProcessor(), Measurements.MEAN, null);
                        resultsTable.addValue("Mean (DB)", (float) isDBMean.mean);
                        ImageStatistics isMinMaxDA = ImageStatistics.getStatistics(donorAfter.getProcessor(), Measurements.MIN_MAX, null);
                        resultsTable.addValue("Min (DA)", (float) isMinMaxDA.min);
                        resultsTable.addValue("Max (DA)", (float) isMinMaxDA.max);
                        ImageStatistics isDAMean = ImageStatistics.getStatistics(donorAfter.getProcessor(), Measurements.MEAN, null);
                        resultsTable.addValue("Mean (DA)", (float) isDAMean.mean);
                        ImageStatistics isABMean = null;
                        if (acceptorBefore != null) {
                            ImageStatistics isMinMaxAB = ImageStatistics.getStatistics(acceptorBefore.getProcessor(), Measurements.MIN_MAX, null);
                            resultsTable.addValue("Min (AB)", (float) isMinMaxAB.min);
                            resultsTable.addValue("Max (AB)", (float) isMinMaxAB.max);
                            isABMean = ImageStatistics.getStatistics(acceptorBefore.getProcessor(), Measurements.MEAN, null);
                            resultsTable.addValue("Mean (AB)", (float) isABMean.mean);
                        } else {
                            resultsTable.addValue("Min (AB)", (float) 0);
                            resultsTable.addValue("Max (AB)", (float) 0);
                            resultsTable.addValue("Mean (AB)", (float) 0);
                        }
                        analyzer.displayResults();
                        analyzer.updateHeadings();
                    }
                }
                donorBefore.changes = false;
                donorAfter.changes = false;
                if (acceptorBefore != null) {
                    acceptorBefore.changes = false;
                }
                if (acceptorAfter != null) {
                    acceptorAfter.changes = false;
                }
            } else if (e.getActionCommand().equals("measureFretImage")) {
                float donorBlCorr = 1;
                float acceptorCTCorr = 0;
                float acceptorPPCorr = 0;
                if (transferImage == null) {
                    logError("Transfer image required.");
                    return;
                }
                resultsTable.incrementCounter();
                int width = transferImage.getWidth();
                int height = transferImage.getHeight();
                int currentRow = resultsTable.getCounter();
                if (currentlyProcessedFileName != null) {
                    resultsTable.setValue("File", currentRow, currentlyProcessedFileName);
                }
                if (transferImage.getRoi() != null) {
                    Roi roi = transferImage.getRoi();
                    int count = 0;
                    int notNan = 0;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (roi.contains(i, j)) {
                                count++;
                                if (transferImage.getProcessor().getPixelValue(i, j) >= -1) {
                                    notNan++;
                                }
                            }
                        }
                    }
                    resultsTable.addValue("Pixels", count);
                    resultsTable.addValue("Not NaN p.", notNan);
                } else {
                    int notNan = 0;
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            if (transferImage.getProcessor().getPixelValue(i, j) >= -1) {
                                notNan++;
                            }
                        }
                    }
                    resultsTable.addValue("Pixels", width * height);
                    resultsTable.addValue("Not NaN p.", notNan);
                }
                ImageStatistics isMean = ImageStatistics.getStatistics(transferImage.getProcessor(), Measurements.MEAN, null);
                resultsTable.addValue("Mean", (float) isMean.mean);
                ImageStatistics isMedian = ImageStatistics.getStatistics(transferImage.getProcessor(), Measurements.MEDIAN, null);
                resultsTable.addValue("Median", (float) isMedian.median);
                ImageStatistics isStdDev = ImageStatistics.getStatistics(transferImage.getProcessor(), Measurements.STD_DEV, null);
                resultsTable.addValue("Std. dev.", (float) isStdDev.stdDev);
                ImageStatistics isMinMax = ImageStatistics.getStatistics(transferImage.getProcessor(), Measurements.MIN_MAX, null);
                resultsTable.addValue("Min", (float) isMinMax.min);
                resultsTable.addValue("Max", (float) isMinMax.max);
                if (transferImage.getRoi() != null) {
                    donorBefore.setRoi(transferImage.getRoi());
                    donorAfter.setRoi(transferImage.getRoi());
                    if (acceptorBefore != null) {
                        acceptorBefore.setRoi(transferImage.getRoi());
                    }
                } else {
                    donorBefore.killRoi();
                    donorAfter.killRoi();
                    if (acceptorBefore != null) {
                        acceptorBefore.killRoi();
                    }
                }
                ImageStatistics isMinMaxDB = ImageStatistics.getStatistics(donorBefore.getProcessor(), Measurements.MIN_MAX, null);
                resultsTable.addValue("Min (DB)", (float) isMinMaxDB.min);
                resultsTable.addValue("Max (DB)", (float) isMinMaxDB.max);
                ImageStatistics isDBMean = ImageStatistics.getStatistics(donorBefore.getProcessor(), Measurements.MEAN, null);
                resultsTable.addValue("Mean (DB)", (float) isDBMean.mean);
                ImageStatistics isMinMaxDA = ImageStatistics.getStatistics(donorAfter.getProcessor(), Measurements.MIN_MAX, null);
                resultsTable.addValue("Min (DA)", (float) isMinMaxDA.min);
                resultsTable.addValue("Max (DA)", (float) isMinMaxDA.max);
                ImageStatistics isDAMean = ImageStatistics.getStatistics(donorAfter.getProcessor(), Measurements.MEAN, null);
                resultsTable.addValue("Mean (DA)", (float) isDAMean.mean);
                ImageStatistics isABMean = null;
                if (acceptorBefore != null) {
                    ImageStatistics isMinMaxAB = ImageStatistics.getStatistics(acceptorBefore.getProcessor(), Measurements.MIN_MAX, null);
                    resultsTable.addValue("Min (AB)", (float) isMinMaxAB.min);
                    resultsTable.addValue("Max (AB)", (float) isMinMaxAB.max);
                    isABMean = ImageStatistics.getStatistics(acceptorBefore.getProcessor(), Measurements.MEAN, null);
                    resultsTable.addValue("Mean (AB)", (float) isABMean.mean);
                } else {
                    resultsTable.addValue("Min (AB)", (float) 0);
                    resultsTable.addValue("Max (AB)", (float) 0);
                    resultsTable.addValue("Mean (AB)", (float) 0);
                }
                analyzer.displayResults();
                analyzer.updateHeadings();
            } else if (e.getActionCommand().equals("semiAutomaticProcessing")) {
                int choice = JOptionPane.showConfirmDialog(this, "Semi-automatic processing of images\n\nOpens and processes FRET images in a given directory. It works with\n"
                        + "Zeiss LSM images (tested with LSM 510 Version 4.0), which contain two\n"
                        + "channels:\n"
                        + "1. donor channel (before and after photobleaching)\n"
                        + "2. acceptor channel (before and after photobleaching)\n\n"
                        + "The upper left corner (1/6 x 1/6 of the image) is considered as background.\n"
                        + "Threshold settings, creation of FRET image and measurements have to be\n"
                        + "made manually.\n\n"
                        + "Every previously opened image and result window will be closed when you\n"
                        + "press \"Ok\".\n\n"
                        + "Press \"Ok\" to select the directory. To continue with the next "
                        + "image, do\nnot close any windows, just press the \"Next\" button.\n", "Semi-automatic processing of images", JOptionPane.OK_CANCEL_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    currentlyProcessedFile = 0;
                    automaticallyProcessedFiles = null;
                    currentlyProcessedFileName = null;
                    WindowManager.closeAllWindows();
                    JFileChooser chooser = new JFileChooser(currentDirectory);
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    chooser.setDialogTitle("Select directory");
                    chooser.setAcceptAllFileFilterUsed(false);
                    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                        log("Processing files in directory: " + chooser.getSelectedFile());
                        currentDirectory = chooser.getSelectedFile().toString();
                    } else {
                        log("Semi-automatic processing: no directory is selected.");
                        return;
                    }
                    nextButton.setVisible(true);
                    useLsmImages.setSelected(true);
                    logScrollPane.setPreferredSize(new Dimension(10, 10));
                    automaticallyProcessedFiles = chooser.getSelectedFile().listFiles();
                    processFile(0);
                }
            } else if (e.getActionCommand().equals("nextImage")) {
                if (transferImage != null) {
                    transferImage.changes = false;
                    transferImage.close();
                }
                if (donorBefore != null) {
                    donorBefore.changes = false;
                    donorBefore.close();
                }
                if (donorAfter != null) {
                    donorAfter.changes = false;
                    donorAfter.close();
                }
                if (acceptorBefore != null) {
                    acceptorBefore.changes = false;
                    acceptorBefore.close();
                }
                if (acceptorAfter != null) {
                    acceptorAfter.changes = false;
                    acceptorAfter.close();
                }
                if (!useAcceptorAsMask.isSelected()) {
                    IJ.selectWindow("Results");
                    WindowManager.putBehind();
                    if (WindowManager.getCurrentImage() != null) {
                        WindowManager.getCurrentImage().close();
                    }
                }
                processFile(++currentlyProcessedFile);
            } else if (e.getActionCommand().equals("closeImages")) {
                if (transferImage != null) {
                    transferImage.changes = false;
                    transferImage.close();
                }
                if (donorBefore != null) {
                    donorBefore.changes = false;
                    donorBefore.close();
                }
                if (donorAfter != null) {
                    donorAfter.changes = false;
                    donorAfter.close();
                }
                if (acceptorBefore != null) {
                    acceptorBefore.changes = false;
                    acceptorBefore.close();
                }
                if (acceptorAfter != null) {
                    acceptorAfter.changes = false;
                    acceptorAfter.close();
                }
                resetAll();
            } else if (e.getActionCommand().equals("resetImages")) {
                resetAll();
            } else if (e.getActionCommand().equals("help")) {
                if (helpWindow != null) {
                    helpWindow.setVisible(false);
                    helpWindow.dispose();
                }
                helpWindow = new HelpWindow(this);
                helpWindow.setVisible(true);
            } else if (e.getActionCommand().equals("checkVersion")) {
                InputStream is = null;
                try {
                    URL url = new URL("http://biophys.med.unideb.hu/accpbfret/version.txt");
                    byte[] buffer = new byte[4];
                    URLConnection urlCon = url.openConnection();
                    is = urlCon.getInputStream();
                    String ver = "";
                    while (is.read(buffer) != -1) {
                        ver += new String(buffer);
                    }
                    float verf = Float.parseFloat(ver);
                    if (verf > version) {
                        int choice = JOptionPane.showConfirmDialog(this, "There is a newer version on the AccPbFRET homepage.\n"
                                + "Your version: " + version + "\n"
                                + "New version: " + ver + "\n"
                                + "You can download it from: http://biophys.med.unideb.hu/accpbfret/ \n"
                                + "Do you want to download it now?", "Checking for latest version", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.YES_OPTION) {
                            IJ.runPlugIn("ij.plugin.BrowserLauncher", "http://biophys.med.unideb.hu/accpbfret/");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "There is no newer version on the AccPbFRET homepage.\n"
                                + "Your version: " + version + "\n"
                                + "Version on the server: " + ver, "Checking for latest version", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e1) {
                    logException(e1.toString(), e1);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (Exception e2) {
                        logException(e2.getMessage(), e2);
                    }
                }
            } else if (e.getActionCommand().equals("about")) {
                JOptionPane optionPane = new JOptionPane();
                optionPane.setMessage("AccPbFRET - an ImageJ plugin for analysis of acceptor photobleaching FRET images\n"
                        + "Homepage: http://biophys.med.unideb.hu/accpbfret/\n"
                        + "Written by: János Roszik (janosr@med.unideb.hu), János Szöllősi (szollo@med.unideb.hu),\n"
                        + "and György Vereb (vereb@med.unideb.hu)\n"
                        + "Version: " + version + " (" + lastModified + ")\n"
                        + "The plugin was tested with ImageJ version " + imageJVersion + " using Java " + javaVersion + ".\n\n"
                        + "If you are using the plugin, please cite the following paper:\n"
                        + "Roszik J, Szollosi J, Vereb G: AccPbFRET: an ImageJ plugin for semi-automatic, fully corrected \n"
                        + "analysis of acceptor photobleaching FRET images. BMC Bioinformatics 2008, 9:346");
                optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = optionPane.createDialog(this, "About");
                dialog.setVisible(true);
            }
        } catch (Throwable t) {
            logException(t.toString(), t);
        }
    }

    private void processFile(int currentFile) {
        resetAllButtonColors();
        if (currentFile >= automaticallyProcessedFiles.length) {
            log("Processing files has been finished.");
            nextButton.setVisible(false);
            logScrollPane.setPreferredSize(new Dimension(10, 10));
            IJ.selectWindow("Results");
            currentlyProcessedFile = 0;
            automaticallyProcessedFiles = null;
            currentlyProcessedFileName = null;
            return;
        }
        if (!automaticallyProcessedFiles[currentFile].isFile() || !(automaticallyProcessedFiles[currentFile].getName().endsWith(".lsm") || automaticallyProcessedFiles[currentFile].getName().endsWith(".LSM"))) {
            processFile(++currentlyProcessedFile);
            return;
        }
        log("Current file is: " + automaticallyProcessedFiles[currentFile].getName());
        currentlyProcessedFileName = automaticallyProcessedFiles[currentFile].getName();
        (new Opener()).open(automaticallyProcessedFiles[currentFile].getAbsolutePath());
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "split"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setAcceptorAfter"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "lutSpectrum"));
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setDonorAfter"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "lutSpectrum"));
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setAcceptorBefore"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "lutSpectrum"));
        WindowManager.putBehind();
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setDonorBefore"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "lutSpectrum"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "registerImages"));
        donorBefore.setRoi(new Roi(0, 0, donorBefore.getWidth() / 6, donorBefore.getHeight() / 6));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "copyRoi"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "subtractDonorBefore"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "subtractDonorAfter"));
        if (partialBlCorrMenuItem.isSelected()) {
            this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "subtractAcceptorBefore"));
            this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "subtractAcceptorAfter"));
        } else if (useAcceptorAsMask.isSelected()) {
            this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "subtractAcceptorBefore"));
        }
        donorBefore.setRoi(new Roi(0, 0, donorBefore.getWidth() / 6, donorBefore.getHeight() / 6));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "copyRoi"));
        donorBefore.getProcessor().setValue(0);
        donorBefore.getProcessor().fill();
        donorAfter.getProcessor().setValue(0);
        donorAfter.getProcessor().fill();
        if (partialBlCorrMenuItem.isSelected()) {
            acceptorBefore.getProcessor().setValue(0);
            acceptorBefore.getProcessor().fill();
            acceptorAfter.getProcessor().setValue(0);
            acceptorAfter.getProcessor().fill();
        } else if (useAcceptorAsMask.isSelected()) {
            acceptorBefore.getProcessor().setValue(0);
            acceptorBefore.getProcessor().fill();
        }
        donorBefore.killRoi();
        donorAfter.killRoi();
        if (partialBlCorrMenuItem.isSelected()) {
            acceptorBefore.killRoi();
            acceptorAfter.killRoi();
        } else if (useAcceptorAsMask.isSelected()) {
            acceptorBefore.killRoi();
        }
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "smoothDBefore"));
        this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "smoothDAfter"));
        if (partialBlCorrMenuItem.isSelected()) {
            this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "smoothABefore"));
            this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "smoothAAfter"));
        } else if (useAcceptorAsMask.isSelected()) {
            this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "smoothABefore"));
        }
    }

    private void resetAll() {
        donorBefore = null;
        donorBeforeSave = null;
        donorAfter = null;
        donorAfterSave = null;
        acceptorBefore = null;
        acceptorBeforeSave = null;
        acceptorAfter = null;
        acceptorAfterSave = null;
        setDonorBeforeButton.setBackground(originalButtonColor);
        setDonorAfterButton.setBackground(originalButtonColor);
        setAcceptorBeforeButton.setBackground(originalButtonColor);
        setAcceptorAfterButton.setBackground(originalButtonColor);
        registerButton.setBackground(originalButtonColor);
        subtractDonorBeforeButton.setBackground(originalButtonColor);
        subtractDonorAfterButton.setBackground(originalButtonColor);
        subtractAcceptorBeforeButton.setBackground(originalButtonColor);
        subtractAcceptorAfterButton.setBackground(originalButtonColor);
        smoothDonorBeforeButton.setBackground(originalButtonColor);
        smoothDonorAfterButton.setBackground(originalButtonColor);
        smoothAcceptorBeforeButton.setBackground(originalButtonColor);
        smoothAcceptorAfterButton.setBackground(originalButtonColor);
        thresholdDonorBeforeButton.setBackground(originalButtonColor);
        thresholdDonorAfterButton.setBackground(originalButtonColor);
        thresholdAcceptorBeforeButton.setBackground(originalButtonColor);
        thresholdAcceptorAfterButton.setBackground(originalButtonColor);
        calculateDBCorrButton.setBackground(originalButtonColor);
        calculateAccCTCorrButton.setBackground(originalButtonColor);
        calculateAccPPCorrButton.setBackground(originalButtonColor);
        calculatePartialBlCorrButton.setBackground(originalButtonColor);

        nextButton.setVisible(false);
        logScrollPane.setPreferredSize(new Dimension(10, 10));
        currentlyProcessedFile = 0;
        automaticallyProcessedFiles = null;
        currentlyProcessedFileName = null;
    }

    private void resetAllButtonColors() {
        setDonorBeforeButton.setBackground(originalButtonColor);
        setDonorAfterButton.setBackground(originalButtonColor);
        setAcceptorBeforeButton.setBackground(originalButtonColor);
        setAcceptorAfterButton.setBackground(originalButtonColor);
        registerButton.setBackground(originalButtonColor);
        subtractDonorBeforeButton.setBackground(originalButtonColor);
        subtractDonorAfterButton.setBackground(originalButtonColor);
        subtractAcceptorBeforeButton.setBackground(originalButtonColor);
        subtractAcceptorAfterButton.setBackground(originalButtonColor);
        smoothDonorBeforeButton.setBackground(originalButtonColor);
        smoothDonorAfterButton.setBackground(originalButtonColor);
        smoothAcceptorBeforeButton.setBackground(originalButtonColor);
        smoothAcceptorAfterButton.setBackground(originalButtonColor);
        thresholdDonorBeforeButton.setBackground(originalButtonColor);
        thresholdDonorAfterButton.setBackground(originalButtonColor);
        thresholdAcceptorBeforeButton.setBackground(originalButtonColor);
        thresholdAcceptorAfterButton.setBackground(originalButtonColor);
        calculateDBCorrButton.setBackground(originalButtonColor);
        calculateAccCTCorrButton.setBackground(originalButtonColor);
        calculateAccPPCorrButton.setBackground(originalButtonColor);
        calculatePartialBlCorrButton.setBackground(originalButtonColor);
    }

    public void log(String text) {
        try {
            log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " " + text, log.getStyle("BLACK"));
            log.setCaretPosition(log.getDocument().getLength());
        } catch (javax.swing.text.BadLocationException e) {
        }
    }

    public void logError(String text) {
        try {
            log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " ERROR: " + text, log.getStyle("RED"));
            log.setCaretPosition(log.getDocument().getLength());
        } catch (javax.swing.text.BadLocationException e) {
        }
    }

    public void logWarning(String text) {
        try {
            log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " WARNING: " + text, log.getStyle("BLUE"));
            log.setCaretPosition(log.getDocument().getLength());
        } catch (javax.swing.text.BadLocationException e) {
        }
    }

    public void logException(String message, Throwable t) {
        try {
            if (debugMenuItem.isSelected()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                pw.flush();
                log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " ERROR: " + sw.toString(), log.getStyle("RED"));
                log.setCaretPosition(log.getDocument().getLength());
            } else {
                log.getDocument().insertString(log.getDocument().getLength(), "\n" + format.format(new Date()) + " ERROR: " + message, log.getStyle("RED"));
                log.setCaretPosition(log.getDocument().getLength());
            }
        } catch (javax.swing.text.BadLocationException e) {
        }
    }

    public void exit() {
        int choice = JOptionPane.showConfirmDialog(this, "Do you really want to exit?", "Exit", JOptionPane.OK_CANCEL_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (shiftDialog != null) {
                shiftDialog.setVisible(false);
                shiftDialog.dispose();
            }
            if (donorBlCorrDialog != null) {
                donorBlCorrDialog.setVisible(false);
                donorBlCorrDialog.dispose();
            }
            if (acceptorCTCorrDialog != null) {
                acceptorCTCorrDialog.setVisible(false);
                acceptorCTCorrDialog.dispose();
            }
            if (applyMaskDialog != null) {
                applyMaskDialog.setVisible(false);
                applyMaskDialog.dispose();
            }
            if (calculateImgRatioDialog != null) {
                calculateImgRatioDialog.setVisible(false);
                calculateImgRatioDialog.dispose();
            }
            if (helpWindow != null) {
                helpWindow.setVisible(false);
                helpWindow.dispose();
            }
            setVisible(false);
            dispose();
        }
    }

    public void windowClosing(WindowEvent e) {
        exit();
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public ImagePlus getDonorBefore() {
        return donorBefore;
    }

    public ImagePlus getDonorAfter() {
        return donorAfter;
    }

    public void setBleachingCorrection(String value) {
        donorBlCorrField.setText(value);
    }

    public void setCrosstalkCorrection(String value) {
        accCrossTalkCorrField.setText(value);
    }

    public void setPhotoproductCorrection(String value) {
        accPhotoprCorrField.setText(value);
    }

    public static void main(String args[]) {
        new AccPbFRET_Plugin();
    }

}
