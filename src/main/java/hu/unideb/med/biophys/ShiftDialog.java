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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ColorModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 *
 */
public class ShiftDialog extends JDialog implements ActionListener {

    private AccPbFRET_Plugin accBlWindow;
    JPanel panel;
    JButton leftButton, rightButton, upButton, downButton;
    JButton cancelButton = new JButton("Close");

    public ShiftDialog(AccPbFRET_Plugin accBlWindow) {
        setTitle("32-bit Image Shifter");
        this.accBlWindow = accBlWindow;
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setModal(false);
        createDialogGui();
        getRootPane().setDefaultButton(cancelButton);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(150, 150);
        setLocation((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2);
    }

    public void createDialogGui() {
        GridBagLayout gridbaglayout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        panel = new JPanel();
        panel.setLayout(gridbaglayout);

        gc.insets = new Insets(0, 0, 0, 0);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = 1;
        gc.gridx = 1;
        gc.gridy = 0;
        upButton = new JButton("^");
        upButton.addActionListener(this);
        upButton.setActionCommand("up");
        panel.add(upButton, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        leftButton = new JButton("<");
        leftButton.addActionListener(this);
        leftButton.setActionCommand("left");
        panel.add(leftButton, gc);
        gc.gridx = 2;
        gc.gridy = 1;
        rightButton = new JButton(">");
        rightButton.addActionListener(this);
        rightButton.setActionCommand("right");
        panel.add(rightButton, gc);
        gc.insets = new Insets(2, 2, 2, 2);
        gc.gridx = 1;
        gc.gridy = 2;
        downButton = new JButton("v");
        downButton.addActionListener(this);
        downButton.setActionCommand("down");
        panel.add(downButton, gc);
        gc.insets = new Insets(0, 0, 4, 0);
        gc.gridwidth = 3;
        gc.gridx = 0;
        gc.gridy = 3;
        panel.add(cancelButton, gc);
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("cancel");

        getContentPane().add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (e.getActionCommand()) {
                case "cancel":
                    setVisible(false);
                    break;
                case "up": {
                    ImagePlus image = WindowManager.getCurrentImage();
                    shiftUp(image, 1);
                    break;
                }
                case "down": {
                    ImagePlus image = WindowManager.getCurrentImage();
                    shiftDown(image, 1);
                    break;
                }
                case "left": {
                    ImagePlus image = WindowManager.getCurrentImage();
                    shiftLeft(image, 1);
                    break;
                }
                case "right": {
                    ImagePlus image = WindowManager.getCurrentImage();
                    shiftRight(image, 1);
                    break;
                }
                default:
                    break;
            }
        } catch (Throwable t) {
            accBlWindow.logException(t.toString(), t);
        }
    }

    public void shiftUp(ImagePlus image, int value) {
        if (image == null) {
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        FloatProcessor fp = (FloatProcessor) image.getProcessor();
        ColorModel cm = fp.getColorModel();

        float[] fpPixels = (float[]) fp.getPixels();
        float[][] fpPixels2 = new float[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height - value; j++) {
                fpPixels2[i][j] = fpPixels[width * (j + value) + i];
            }
        }
        FloatProcessor newFp = new FloatProcessor(fpPixels2);
        newFp.setColorModel(cm);
        image.setProcessor(image.getTitle(), newFp);
        image.updateAndDraw();
    }

    public void shiftDown(ImagePlus image, int value) {
        if (image == null) {
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        FloatProcessor fp = (FloatProcessor) image.getProcessor();
        ColorModel cm = fp.getColorModel();

        float[] fpPixels = (float[]) fp.getPixels();
        float[][] fpPixels2 = new float[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = value; j < height; j++) {
                fpPixels2[i][j] = fpPixels[width * (j - value) + i];
            }
        }
        FloatProcessor newFp = new FloatProcessor(fpPixels2);
        newFp.setColorModel(cm);
        image.setProcessor(image.getTitle(), newFp);
        image.updateAndDraw();
    }

    public void shiftLeft(ImagePlus image, int value) {
        if (image == null) {
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        FloatProcessor fp = (FloatProcessor) image.getProcessor();
        ColorModel cm = fp.getColorModel();

        float[] fpPixels = (float[]) fp.getPixels();
        float[][] fpPixels2 = new float[width][height];
        for (int i = 0; i < width - value; i++) {
            for (int j = 0; j < height; j++) {
                fpPixels2[i][j] = fpPixels[width * j + (i + value)];
            }
        }
        FloatProcessor newFp = new FloatProcessor(fpPixels2);
        newFp.setColorModel(cm);
        image.setProcessor(image.getTitle(), newFp);
        image.updateAndDraw();
    }

    public void shiftRight(ImagePlus image, int value) {
        if (image == null) {
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        FloatProcessor fp = (FloatProcessor) image.getProcessor();
        ColorModel cm = fp.getColorModel();

        float[] fpPixels = (float[]) fp.getPixels();
        float[][] fpPixels2 = new float[width][height];
        for (int i = value; i < width; i++) {
            for (int j = 0; j < height; j++) {
                fpPixels2[i][j] = fpPixels[width * j + (i - value)];
            }
        }
        FloatProcessor newFp = new FloatProcessor(fpPixels2);
        newFp.setColorModel(cm);
        image.setProcessor(image.getTitle(), newFp);
        image.updateAndDraw();
    }
}
