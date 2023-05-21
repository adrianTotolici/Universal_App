package org.homemade.stockmanager.graphics;

import javax.swing.*;
import java.awt.*;

public class GraphDisplay extends JPanel {
    private double[] data; // Array of data points

    public GraphDisplay(double[] data) {
        this.data = data;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Set up the graph's properties
        int graphWidth = getWidth() - 40;
        int graphHeight = getHeight() - 40;
        int xPadding = 20;
        int yPadding = 20;

        // Draw the x-axis
        g.drawLine(xPadding, getHeight() - yPadding, xPadding + graphWidth, getHeight() - yPadding);

        // Draw the y-axis
        g.drawLine(xPadding, getHeight() - yPadding, xPadding, yPadding);

        // Calculate the width of each bar
        int barWidth = graphWidth / data.length;

        // Draw the bars and labels
        int x = xPadding;
        for (int i = 0; i < data.length; i++) {
            int barHeight = (int) (data[i] / getMaxValue(data) * (graphHeight - yPadding));
            int y = getHeight() - yPadding - barHeight;
            g.fillRect(x, y, barWidth, barHeight);

            // Display x and y values
            g.setColor(Color.BLACK);
            String valueLabel = String.format("%.2f", data[i]);
            int labelX = x + barWidth / 2 - g.getFontMetrics().stringWidth(valueLabel) / 2;
            int labelY = y - 5;
            g.drawString(valueLabel, labelX, labelY);

            // Increment x for the next bar
            x += barWidth;
        }
    }

    private double getMaxValue(double[] array) {
        double max = Double.MIN_VALUE;
        for (double value : array) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}