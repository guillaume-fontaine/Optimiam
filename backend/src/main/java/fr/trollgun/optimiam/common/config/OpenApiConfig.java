package fr.trollgun.optimiam.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI optimiamOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OptiMiam API")
                        .description("API REST pour la gestion intelligente des produits alimentaires, des stocks, des recommandations, du planning et des courses.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Équipe OptiMiam")
                                .email("contact@optimiam.local"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
