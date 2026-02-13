package org.zhadaev.reality;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/crutch")
public class CrutchController {
    @GetMapping
    public void emptyApi() {}
}
