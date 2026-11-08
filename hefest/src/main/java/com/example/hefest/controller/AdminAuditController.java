package com.example.hefest.controller;

import com.example.hefest.service.DataAuditService;
import com.example.hefest.service.SessionTrackingService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.time.LocalDate;

/** Admin-only (see SecurityConfig: /admin/**) overview of invalid rows and finished login sessions. */
@Controller
@RequestMapping("/admin/audit")
public class AdminAuditController {

    private final DataAuditService dataAuditService;
    private final SessionTrackingService sessionTrackingService;
    private final com.example.hefest.service.AuditLogService auditLogService;

    public AdminAuditController(DataAuditService dataAuditService,
                                SessionTrackingService sessionTrackingService,
                                com.example.hefest.service.AuditLogService auditLogService) {
        this.dataAuditService = dataAuditService;
        this.sessionTrackingService = sessionTrackingService;
        this.auditLogService = auditLogService;
    }


    @GetMapping
    public String audit(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) String actor,
                        @RequestParam(required = false) String entityType,
                        @RequestParam(required = false) LocalDate from,
                        @RequestParam(required = false) LocalDate to,
                        Model model) {
        List<DataAuditService.TableCheck> checks = dataAuditService.checkAllTables();
        Page<SessionTrackingService.SessionView> sessions = sessionTrackingService.pastSessions(page, size);

        model.addAttribute("checks", checks);
        model.addAttribute("invalidTotal", checks.stream().mapToInt(c -> c.invalid().size()).sum());
        model.addAttribute("checkedTotal", checks.stream().mapToInt(DataAuditService.TableCheck::checked).sum());
        model.addAttribute("sessions", sessions);
        model.addAttribute("pastSessionCount", sessionTrackingService.pastCount());
        model.addAttribute("activeSessionCount", sessionTrackingService.activeCount());
        model.addAttribute("size", sessions.getSize());
        model.addAttribute("auditLogs", auditLogService.search(actor, entityType, from, to, page, size));
        model.addAttribute("actor", actor == null ? "" : actor);
        model.addAttribute("entityType", entityType == null ? "" : entityType);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/audit";
    }
}
