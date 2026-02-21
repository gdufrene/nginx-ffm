package poc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan
@EnableWebMvc
public class AppConfig {

}

@RestController
class MyController {
	
	@GetMapping(value="/hello", produces="text/plain")
	public String hello() {
		return "Hello from Spring Boot!";
	}
	
}