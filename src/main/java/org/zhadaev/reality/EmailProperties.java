package org.zhadaev.reality;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("email")
public record EmailProperties(
    String host,
    String port,
    String login,
    String password,
    int necessaryCount
) {}
