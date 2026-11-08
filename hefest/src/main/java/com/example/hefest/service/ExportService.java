package com.example.hefest.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ExportService {
    public byte[] csv(List<ReportService.EventReport> reports) {
        StringBuilder csv = new StringBuilder("\uFEFFСъбитие;Дата;Час;Цена;Регистрации;Капацитет;Свободни места;Приход\n");
        for (var report : reports) {
            csv.append(row(report.title())).append(';')
                    .append(row(report.eventDate())).append(';')
                    .append(row(report.eventTime())).append(';')
                    .append(row(report.price())).append(';')
                    .append(report.registrations()).append(';')
                    .append(report.capacity()).append(';')
                    .append(report.availableSpots()).append(';')
                    .append(row(report.revenue())).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] xlsx(List<ReportService.EventReport> reports) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Събития");
            String[] headers = {"Събитие", "Дата", "Час", "Цена", "Регистрации", "Капацитет", "Свободни места", "Приход"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            for (int rowIndex = 0; rowIndex < reports.size(); rowIndex++) {
                var report = reports.get(rowIndex);
                Row row = sheet.createRow(rowIndex + 1);
                row.createCell(0).setCellValue(report.title());
                row.createCell(1).setCellValue(String.valueOf(report.eventDate()));
                row.createCell(2).setCellValue(report.eventTime() == null ? "" : report.eventTime().toString());
                row.createCell(3).setCellValue(report.price().doubleValue());
                row.createCell(4).setCellValue(report.registrations());
                row.createCell(5).setCellValue(report.capacity());
                row.createCell(6).setCellValue(report.availableSpots());
                row.createCell(7).setCellValue(report.revenue().doubleValue());
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Неуспешно генериране на Excel файл.", e);
        }
    }

    private static String row(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
