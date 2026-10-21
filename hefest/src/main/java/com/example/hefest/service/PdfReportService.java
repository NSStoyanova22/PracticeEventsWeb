package com.example.hefest.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.hefest.service.ReportService.EventReport;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service 
public class PdfReportService {

    private final ReportService reportService;

    public PdfReportService(ReportService reportService) {
        this.reportService = reportService;
    }
    public byte[] generatePdfReport(String title, LocalDate from, LocalDate to,
                                    String week, String sort, String direction) {
        List<EventReport> reports = reportService.generateReport(from, to, week, sort, direction);
     ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph(title));
            document.add(new Paragraph(
                    "Period: " + from + " - " + to
            ));
            document.add(new Paragraph(
                    "Number of events: " + reports.size()
            ));
            document.add(new Paragraph(
                    "Total revenue: " + reports.stream().map(EventReport::revenue)
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add) + " BGN"
            ));
            PdfPTable table = new PdfPTable(7);
            table.addCell("Event");
            table.addCell("Date");
            table.addCell("Time");
            table.addCell("Price");
            table.addCell("Registrations");
            table.addCell("Capacity");
            table.addCell("Available spots");
            table.addCell("Revenue");
            for (EventReport report : reports) {
                table.addCell(report.title());
                table.addCell(report.eventDate().toString());
                table.addCell(report.eventTime() == null ? "-" : report.eventTime().toString());
                table.addCell(report.price() + " BGN");
                table.addCell(
                        String.valueOf(report.registrations())
                );
                table.addCell(
                        String.valueOf(report.capacity())
                );
                table.addCell(
                        String.valueOf(report.availableSpots())
                );
                table.addCell(report.revenue() + " BGN");
            }
            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException(
                    "Could not generate PDF report", e
            );
        }
    }

}
