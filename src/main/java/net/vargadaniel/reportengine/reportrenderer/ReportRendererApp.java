package net.vargadaniel.reportengine.reportrenderer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@EnableResourceServer
@EnableOAuth2Client
public class ReportRendererApp {

	public static void main(String... args) {
		String k8sNamespace = System.getenv("KUBERNETES_NAMESPACE");
		if (k8sNamespace != null) {
			String profile = k8sNamespace.substring(k8sNamespace.lastIndexOf("-") + 1);
			System.setProperty("spring.profiles.active", profile);
		}
		SpringApplication.run(ReportRendererApp.class, args);
	}
	
}