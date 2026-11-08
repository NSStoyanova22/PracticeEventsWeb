package com.example.hefest.controller;
import com.example.hefest.model.User;
import com.example.hefest.service.CollectionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class CollectionController {
    private final CollectionService service;
    public CollectionController(CollectionService service) { this.service=service; }
    @GetMapping("/my-lists") public String lists(@AuthenticationPrincipal User user, Model model) { model.addAttribute("favorites",service.favorites(user)); model.addAttribute("collections",service.collections(user)); return "my-lists"; }
    @PostMapping("/favorites/{eventId}") public String favorite(@PathVariable Long eventId,@AuthenticationPrincipal User user){service.toggleFavorite(eventId,user);return "redirect:/events/"+eventId;}
    @PostMapping("/collections") public String create(@RequestParam String name,@AuthenticationPrincipal User user,RedirectAttributes redirect){try{service.create(name,user);}catch(RuntimeException e){redirect.addFlashAttribute("listError",e.getMessage());}return "redirect:/my-lists";}
    @PostMapping("/collections/{collectionId}/events/{eventId}") public String add(@PathVariable Long collectionId,@PathVariable Long eventId,@AuthenticationPrincipal User user){service.addToCollection(collectionId,eventId,user);return "redirect:/my-lists";}
    @GetMapping("/collections/{collectionId}") public String view(@PathVariable Long collectionId,@AuthenticationPrincipal User user,Model model){model.addAttribute("items",service.items(collectionId,user));model.addAttribute("collection",service.collections(user).stream().filter(c->c.getId().equals(collectionId)).findFirst().orElseThrow());return "collection-detail";}
}
