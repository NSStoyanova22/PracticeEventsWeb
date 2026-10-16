package com.example.hefest.controller;

import java.time.LocalDate;

import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.hefest.service.ReportService;
import com.example.hefest.service.PdfReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
public class ReportController {

    private final ReportService reportService;
    private final PdfReportService pdfReportService;

    public ReportController(ReportService reportService, PdfReportService pdfReportService) {
        this.reportService = reportService;
        this.pdfReportService = pdfReportService;
    }
    

    @GetMapping("/reports")
    public String reports(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String week,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        if (from == null && to == null) {
            to = LocalDate.now();
            from = to.minusMonths(12);
        }

        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("week", week == null ? "" : week);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        if (from != null && to != null) {
            try {
                var reports = reportService.generateReport(from, to, week, sort, direction);
                long totalRegistrations = reports.stream()
                        .mapToLong(ReportService.EventReport::registrations)
                        .sum();

                model.addAttribute("reports", reports);
                model.addAttribute("numberOfEvents", reports.size());
                model.addAttribute("numberOfRegistered", totalRegistrations);
                model.addAttribute("totalRevenue", reports.stream()
                        .map(ReportService.EventReport::revenue)
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
                model.addAttribute("mostPopularEvent", reports.stream()
                        .max(java.util.Comparator.comparingLong(ReportService.EventReport::registrations))
                        .orElse(null));
            } catch (IllegalArgumentException exception) {
                model.addAttribute("errorMessage", exception.getMessage());
            }
        }

        return "reports";
    }
    @GetMapping ("/reports/pdf")
    public ResponseEntity<byte[]> pdfReports(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String week,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
                if (from == null || to == null) {
                    to = LocalDate.now();
                    from = to.minusMonths(12);
                }
                byte[] pdfReport = pdfReportService.generatePdfReport("Event Report", from, to, week, sort, direction);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDisposition(ContentDisposition.attachment().filename("event-report.pdf").build());
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(pdfReport);


        
    }
}
