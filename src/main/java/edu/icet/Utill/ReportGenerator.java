package edu.icet.Utill;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ReportGenerator {
    
    public static void generateInvoiceReport(Connection connection, Integer invoiceId, boolean showPreview) {
        try {
            // Load the main report template
            InputStream reportStream = ReportGenerator.class.getResourceAsStream("/reports/invoice_report.jasper");
            if (reportStream == null) {
                // If .jasper file doesn't exist, compile the .jrxml file
                String jrxmlPath = ReportGenerator.class.getResource("/reports/invoice_report.jrxml").getPath();
                JasperCompileManager.compileReportToFile(jrxmlPath);
                reportStream = ReportGenerator.class.getResourceAsStream("/reports/invoice_report.jasper");
            }
            
            // Load the subreport template
            InputStream subreportStream = ReportGenerator.class.getResourceAsStream("/reports/invoice_items_subreport.jasper");
            if (subreportStream == null) {
                // If .jasper file doesn't exist, compile the .jrxml file
                String jrxmlPath = ReportGenerator.class.getResource("/reports/invoice_items_subreport.jrxml").getPath();
                JasperCompileManager.compileReportToFile(jrxmlPath);
            }

            // Set report parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("INVOICE_ID", invoiceId);
            parameters.put("LOGO_PATH", ReportGenerator.class.getResource("/images/hotel_logo.png").getPath());
            
            // Fill the report
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            
            // Generate PDF file
            String outputPath = "invoices/invoice_" + invoiceId + ".pdf";
            new File("invoices").mkdirs(); // Create directory if it doesn't exist
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
            
            // Show preview if requested
            if (showPreview) {
                JasperViewer.viewReport(jasperPrint, false);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating invoice report: " + e.getMessage());
        }
    }
    
    public static void generateInvoiceListReport(Connection connection, String startDate, String endDate, String paymentStatus) {
        generateReport(connection, "invoice_list_report", startDate, endDate, paymentStatus);
    }

    public static void generateRevenueReport(Connection connection, String startDate, String endDate) {
        generateReport(connection, "revenue_report", startDate, endDate, null);
    }

    public static void generateProfitLossReport(Connection connection, String startDate, String endDate) {
        generateReport(connection, "profit_loss_report", startDate, endDate, null);
    }

    public static void generateOccupancyReport(Connection connection, String startDate, String endDate) {
        generateReport(connection, "occupancy_report", startDate, endDate, null);
    }

    public static void generateRoomTypeReport(Connection connection, String startDate, String endDate) {
        generateReport(connection, "room_type_report", startDate, endDate, null);
    }

    public static void generateCustomerAnalysisReport(Connection connection, String startDate, String endDate) {
        generateReport(connection, "customer_analysis_report", startDate, endDate, null);
    }

    public static void generateFeedbackReport(Connection connection, String startDate, String endDate) {
        generateReport(connection, "feedback_report", startDate, endDate, null);
    }

    private static void generateReport(Connection connection, String reportName, String startDate, String endDate, String additionalParam) {
        try {
            // Load the report template
            InputStream reportStream = ReportGenerator.class.getResourceAsStream("/reports/" + reportName + ".jasper");
            if (reportStream == null) {
                // If .jasper file doesn't exist, compile the .jrxml file
                String jrxmlPath = ReportGenerator.class.getResource("/reports/" + reportName + ".jrxml").getPath();
                JasperCompileManager.compileReportToFile(jrxmlPath);
                reportStream = ReportGenerator.class.getResourceAsStream("/reports/" + reportName + ".jasper");
            }
            
            // Set report parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("START_DATE", startDate);
            parameters.put("END_DATE", endDate);
            if (additionalParam != null) {
                parameters.put("PAYMENT_STATUS", additionalParam);
            }
            parameters.put("LOGO_PATH", ReportGenerator.class.getResource("/images/hotel_logo.png").getPath());
            
            // Fill the report
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            
            // Generate PDF file
            String outputPath = "reports/" + reportName + "_" + System.currentTimeMillis() + ".pdf";
            new File("reports").mkdirs(); // Create directory if it doesn't exist
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
            
            // Show preview
            JasperViewer.viewReport(jasperPrint, false);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating " + reportName + ": " + e.getMessage());
        }
    }
} 