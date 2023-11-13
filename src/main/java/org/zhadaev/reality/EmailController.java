package org.zhadaev.reality;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/process")
    public void checkEmail() {
        emailService.checkEmail();
    }

    @DeleteMapping("/unnecessary")
    public void removeUnnecessary() {
        emailService.removeUnnecessary();
    }
}
