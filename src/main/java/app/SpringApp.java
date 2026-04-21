package app;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan
public class SpringApp {

}

@RestController
@RequestMapping("/spring")
class HelloSpringController {
	
	@GetMapping("/hello")
	public String hello() {
		return "Hello devoxx from Spring MVC within nginx !!!";
	}
	
}