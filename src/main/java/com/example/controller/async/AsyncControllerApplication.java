package com.example.controller.async;

import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

@EnableAsync
@SpringBootApplication
public class AsyncControllerApplication {

	@RestController
	public static class AsyncController {
		@Autowired MyService myService;

		AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

		@GetMapping("/rest")
		public DeferredResult<String> rest(int idx) {
			DeferredResult<String> dr = new DeferredResult<>();

			ListenableFuture<ResponseEntity<String>> f1 = rt.getForEntity("http://localhost:8081/service1?req={req}", String.class, "hello" + idx);

			f1.addCallback(s1 -> {
				ListenableFuture<ResponseEntity<String>> f2 = rt.getForEntity("http://localhost:8081/service2?req={req}", String.class, s1.getBody());

				f2.addCallback(s2 -> {
					ListenableFuture<String> f3 = myService.work(s2.getBody());

					f3.addCallback(s3 -> {
						dr.setResult(s3);
					}, e -> {
						dr.setErrorResult(e.getMessage());
					});
				}, e -> {
					dr.setErrorResult(e.getMessage());
				});
			}, e -> {
				dr.setErrorResult(e.getMessage());
			});

			return dr;
		}
	}

	@Service
	public static class MyService {
		@Async
		public ListenableFuture<String> work(String req) {
			return new AsyncResult<>(req + "/asyncwork");
		}
	}

	@Bean
	public ThreadPoolTaskExecutor myThreadPool() {
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		te.setCorePoolSize(1);
		te.setMaxPoolSize(1);
		te.initialize();
		return te;
	}

	public static void main(String[] args) {
		System.getProperties().put("server.port", 8080);
		System.getProperties().put("server.tomcat.max-threads", 1);
		SpringApplication.run(AsyncControllerApplication.class, args);
	}
}
