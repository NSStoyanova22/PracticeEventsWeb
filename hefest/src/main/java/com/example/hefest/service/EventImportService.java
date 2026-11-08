package com.example.hefest.service;

import com.example.hefest.model.Event;
import com.example.hefest.model.User;
import com.example.hefest.repository.EventRepository;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class EventImportService {
    public record ImportRow(int rowNumber, String title, String description, LocalDate date, LocalTime time,
                            BigDecimal price, Integer capacity, List<String> errors) implements java.io.Serializable {
        public boolean valid() { return errors.isEmpty(); }
    }
    public record Preview(List<ImportRow> rows) implements java.io.Serializable {
        public boolean valid() { return !rows.isEmpty() && rows.stream().allMatch(ImportRow::valid); }
    }

    private final EventRepository eventRepository;
    private final EventService eventService;

    public EventImportService(EventRepository eventRepository, EventService eventService) {
        this.eventRepository = eventRepository; this.eventService = eventService;
    }

    public Preview preview(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Изберете CSV или XLSX файл.");
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        try {
            List<String[]> rows = name.endsWith(".xlsx") ? readXlsx(file) : readCsv(file);
            List<ImportRow> result = new ArrayList<>();
            Set<String> inFile = new HashSet<>();
            for (int i = 0; i < rows.size(); i++) {
                String[] r = rows.get(i);
                List<String> errors = new ArrayList<>();
                String title = value(r, 0), description = value(r, 1);
                LocalDate date = date(value(r, 2), errors); LocalTime time = time(value(r, 3), errors);
                BigDecimal price = decimal(value(r, 4), errors); Integer capacity = integer(value(r, 5), errors);
                if (title.isBlank()) errors.add("Липсва заглавие.");
                if (description.isBlank()) errors.add("Липсва описание.");
                if (price != null && price.signum() < 0) errors.add("Цената не може да е отрицателна.");
                if (capacity != null && capacity < 1) errors.add("Капацитетът трябва да е поне 1.");
                String duplicateKey = title.toLowerCase(Locale.ROOT) + "|" + date;
                if (!title.isBlank() && date != null && !inFile.add(duplicateKey)) errors.add("Дубликат в импортирания файл.");
                if (date != null && eventRepository.findByDate(date).stream().anyMatch(e -> e.getTitle().equalsIgnoreCase(title))) errors.add("Такова събитие вече съществува.");
                result.add(new ImportRow(i + 1, title, description, date, time, price, capacity, errors));
            }
            return new Preview(result);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException iae) throw iae;
            throw new IllegalArgumentException("Файлът не може да бъде прочетен.", e);
        }
    }

    @Transactional
    public void commit(Preview preview, User actor) {
        if (preview == null || !preview.valid()) throw new IllegalStateException("Импортът не е записан, защото има грешни редове.");
        for (ImportRow row : preview.rows()) {
            Event event = new Event(); event.setTitle(row.title()); event.setDescription(row.description()); event.setDate(row.date());
            event.setTime(row.time()); event.setPrice(row.price()); event.setCapacity(row.capacity());
            eventService.createEvent(event, actor);
        }
    }

    private static List<String[]> readCsv(MultipartFile file) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line; boolean first = true; char delimiter = ',';
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (first) { delimiter = line.indexOf(';') >= 0 ? ';' : ','; first = false; if (line.toLowerCase(Locale.ROOT).contains("title") || line.toLowerCase(Locale.ROOT).contains("заглав")) continue; }
                rows.add(line.split(java.util.regex.Pattern.quote(String.valueOf(delimiter)), -1));
            }
        }
        return rows;
    }

    private static List<String[]> readXlsx(MultipartFile file) throws Exception {
        List<String[]> rows = new ArrayList<>(); DataFormatter formatter = new DataFormatter();
        try (var workbook = WorkbookFactory.create(file.getInputStream())) {
            var sheet = workbook.getSheetAt(0); boolean first = true;
            for (var row : sheet) {
                String[] values = new String[6]; for (int i = 0; i < values.length; i++) values[i] = formatter.formatCellValue(row.getCell(i));
                if (first) { first = false; if (values[0].toLowerCase(Locale.ROOT).contains("title") || values[0].toLowerCase(Locale.ROOT).contains("заглав")) continue; }
                rows.add(values);
            }
        }
        return rows;
    }
    private static String value(String[] row, int index) { return index < row.length && row[index] != null ? row[index].trim() : ""; }
    private static LocalDate date(String value, List<String> errors) { try { return value.isBlank() ? null : LocalDate.parse(value); } catch (Exception e) { errors.add("Невалидна дата (използвайте yyyy-MM-dd)."); return null; } }
    private static LocalTime time(String value, List<String> errors) { try { return value.isBlank() ? null : LocalTime.parse(value); } catch (Exception e) { errors.add("Невалиден час (използвайте HH:mm)."); return null; } }
    private static BigDecimal decimal(String value, List<String> errors) { try { return value.isBlank() ? BigDecimal.ZERO : new BigDecimal(value.replace(',', '.')); } catch (Exception e) { errors.add("Невалидна цена."); return null; } }
    private static Integer integer(String value, List<String> errors) { try { return value.isBlank() ? null : Integer.valueOf(value); } catch (Exception e) { errors.add("Невалиден капацитет."); return null; } }
}
