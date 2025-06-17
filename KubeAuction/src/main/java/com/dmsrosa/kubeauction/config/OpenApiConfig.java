package com.dmsrosa.kubeauction.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info = @Info(title = "KubeAuction API", version = "v1", description = ""))
public class OpenApiConfig {
}
