package com.example.controller.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SyncControllerApplication {

	@RestController
	public static class SyncController {
		RestTemplate restTemplate = new RestTemplate();

		@GetMapping("/rest")
		public String rest(int idx) {
			// Blocking => 현재 tomcat Thread 가 1이기 때문에, 하나의 응답이 와야 다시 요청을 보내고 이 부분을 되풀이함.
			return restTemplate.getForObject("http://localhost:8081/service?req={req}", String.class, "hello" + idx);
		}
	}

	public static void main(String[] args) {
		System.getProperties().put("server.port", 8080);
		System.getProperties().put("server.tomcat.max-threads", 1);
		SpringApplication.run(SyncControllerApplication.class, args);
	}
}
