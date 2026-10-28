package com.acme.salary.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The Angular app uses client-side routes. When a user reloads or bookmarks one of them, serve
 * index.html and let Angular's router take over. API routes are never matched here.
 */
@Controller
public class SpaForwardingController {

    @GetMapping({"/employees", "/employees/**", "/insights", "/insights/**"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
