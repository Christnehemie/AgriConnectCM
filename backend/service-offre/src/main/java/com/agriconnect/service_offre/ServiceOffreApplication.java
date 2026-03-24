package com.agriconnect.service_offre;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceOffreApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOffreApplication.class, args);
    }
}