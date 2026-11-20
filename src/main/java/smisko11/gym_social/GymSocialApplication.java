package smisko11.gym_social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GymSocialApplication {

	public static void main(String[] args) {
		SpringApplication.run(GymSocialApplication.class, args);
	}

}
