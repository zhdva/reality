package org.zhadaev.reality;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/crutch")
public class CrutchController {

    @Get
    public void emptyApi() {}
}
