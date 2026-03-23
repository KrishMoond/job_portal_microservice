package com.jobportal.user.swagger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerUiRedirectController {

    @GetMapping({"/swagger-ui/index.htm", "/swagger-ui"})
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui/index.html";
    }
}
