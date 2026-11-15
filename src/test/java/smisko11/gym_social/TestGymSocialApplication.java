package smisko11.gym_social;

import org.springframework.boot.SpringApplication;

public class TestGymSocialApplication {

	public static void main(String[] args) {
		SpringApplication.from(GymSocialApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
