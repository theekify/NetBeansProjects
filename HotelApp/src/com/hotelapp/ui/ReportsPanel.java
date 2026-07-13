package com.hotelapp.ui;

import com.hotelapp.service.ReportService;
import com.hotelapp.util.SimpleTheme;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ReportsPanel extends JPanel {

    private final ReportService reportService = new ReportService();

    private JPanel previewContainer;
    private JasperPrint lastGeneratedReport;

    public ReportsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(SimpleTheme.PANEL_BG);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(SimpleTheme.CARD_BG);
        topPanel.setBorder(SimpleTheme.coloredTitledBorder("Monthly Revenue Report", SimpleTheme.PRIMARY));
        JButton generateButton = SimpleTheme.coloredButton("Generate Monthly Revenue Report", SimpleTheme.PRIMARY);
        JButton exportButton = SimpleTheme.coloredButton("Export to PDF", SimpleTheme.SUCCESS);
        exportButton.setEnabled(false);

        generateButton.addActionListener(e -> {
            generateReport();
            exportButton.setEnabled(lastGeneratedReport != null);
        });
        exportButton.addActionListener(e -> exportToPdf());

        topPanel.add(generateButton);
        topPanel.add(exportButton);
        add(topPanel, BorderLayout.NORTH);

        previewContainer = new JPanel(new BorderLayout());
        previewContainer.setBackground(SimpleTheme.CARD_BG);
        previewContainer.setBorder(BorderFactory.createLineBorder(SimpleTheme.PRIMARY, 1));
        JLabel placeholder = new JLabel("Click \"Generate Monthly Revenue Report\" to preview.", SwingConstants.CENTER);
        placeholder.setForeground(SimpleTheme.NEUTRAL);
        previewContainer.add(placeholder, BorderLayout.CENTER);
        add(previewContainer, BorderLayout.CENTER);
    }

    private void generateReport() {
        try {
            lastGeneratedReport = reportService.generateMonthlyRevenueReport();

            previewContainer.removeAll();
            JRViewer viewer = new JRViewer(lastGeneratedReport);
            previewContainer.add(viewer, BorderLayout.CENTER);
            previewContainer.revalidate();
            previewContainer.repaint();

        } catch (JRException ex) {
            JOptionPane.showMessageDialog(this, "Could not generate report: " + ex.getMessage(),
                    "Report Error", JOptionPane.ERROR_MESSAGE);
            System.err.println(ex.getMessage());
        }
    }

    private void exportToPdf() {
        if (lastGeneratedReport == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Monthly_Revenue_Report.pdf"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";

        try {
            reportService.exportToPdf(lastGeneratedReport, path);
            JOptionPane.showMessageDialog(this, "Report exported to:\n" + path);
        } catch (JRException ex) {
            JOptionPane.showMessageDialog(this, "Could not export report: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            System.err.println(ex.getMessage());
        }
    }
}
