package in.bawvpl.Authify.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Authify Backend Running Successfully!";
    }

    @GetMapping("/api/v1.0")
    public String apiRoot() {
        return "API v1.0 Root OK";
    }
}
