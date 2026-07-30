package zm.co.zanaco.tracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Initiatives Tracker API")
                        .description("REST API for managing strategic initiatives, budgets, and cost tracking.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ZANACO Technology")
                                .email("technology@zanaco.co.zm"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://www.zanaco.co.zm")))
                .servers(List.of(
                        new Server().url("/tracker").description("Default server")
                ));
    }
}
