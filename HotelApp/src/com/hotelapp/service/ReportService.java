package com.hotelapp.service;

import com.hotelapp.util.DBConnectionManager;
import net.sf.jasperreports.engine.*;

import java.io.InputStream;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Compiles and fills the Monthly Revenue Jasper report using live data
 * from the database (joins invoices + bookings + guests + rooms + room_types).
 */
public class ReportService {

    private static final String TEMPLATE_PATH = "/reports/monthly_revenue_report.jrxml";

    private final RoomService roomService = new RoomService();

    /**
     * Compiles the .jrxml, fills it against the live DB connection,
     * and returns the filled report ready to preview or export.
     */
    public JasperPrint generateMonthlyRevenueReport() throws JRException {
        try (InputStream templateStream = ReportService.class.getResourceAsStream(TEMPLATE_PATH)) {
            if (templateStream == null) {
                throw new JRException("Report template not found on classpath: " + TEMPLATE_PATH);
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);

            Map<String, Object> params = new HashMap<>();
            params.put("ReportMonth", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            params.put("OccupancyRate", calculateOccupancyRateLabel());

            Connection connection = DBConnectionManager.getInstance().getConnection();
            return JasperFillManager.fillReport(jasperReport, params, connection);

        } catch (java.io.IOException e) {
            throw new JRException("Failed to load report template: " + e.getMessage(), e);
        }
    }

    public void exportToPdf(JasperPrint jasperPrint, String outputPath) throws JRException {
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
    }

    private String calculateOccupancyRateLabel() {
        var rooms = roomService.getAllRooms();
        long total = rooms.size();
        long occupied = rooms.stream()
                .filter(r -> r.getStatus() == com.hotelapp.model.Room.Status.OCCUPIED)
                .count();
        double pct = total == 0 ? 0 : (occupied * 100.0 / total);
        return String.format("%.0f%%", pct);
    }
}
