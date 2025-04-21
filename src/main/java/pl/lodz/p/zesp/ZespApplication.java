package pl.lodz.p.zesp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ZespApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZespApplication.class, args);
	}

}
