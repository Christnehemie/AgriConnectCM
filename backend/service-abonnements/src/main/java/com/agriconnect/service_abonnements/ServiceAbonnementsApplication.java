package com.agriconnect.service_abonnements;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ============================================================
 * ServiceAbonnementsApplication — Point d'entrée
 * ============================================================
 * Port : 8082
 * Même BD que service-utilisateurs.
 * S'enregistre auprès d'Eureka.
 * ============================================================
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceAbonnementsApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServiceAbonnementsApplication.class, args);
	}
}