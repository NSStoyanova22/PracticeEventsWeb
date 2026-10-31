package com.example.hefest.controller;

import com.example.hefest.service.DataAuditService;
import com.example.hefest.service.SessionTrackingService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Admin-only (see SecurityConfig: /admin/**) overview of invalid rows and finished login sessions. */
@Controller
@RequestMapping("/admin/audit")
public class AdminAuditController {

    private final DataAuditService dataAuditService;
    private final SessionTrackingService sessionTrackingService;

    public AdminAuditController(DataAuditService dataAuditService,
                                SessionTrackingService sessionTrackingService) {
        this.dataAuditService = dataAuditService;
        this.sessionTrackingService = sessionTrackingService;
    }

    @GetMapping
    public String audit(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
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
        return "admin/audit";
    }
}
