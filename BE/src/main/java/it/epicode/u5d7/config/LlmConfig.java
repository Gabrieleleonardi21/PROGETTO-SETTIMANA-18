package it.epicode.u5d7.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class LlmConfig {

	/** Un solo RestClient per OpenRouter: base URL, chiave in Authorization e timeout fissati qui, una volta. */
	@Bean
	RestClient llmRestClient(@Value("${app.llm.base-url}") String baseUrl,
			@Value("${app.llm.api-key}") String apiKey,
			@Value("${app.llm.timeout-connessione-secondi}") int timeoutConnessione,
			@Value("${app.llm.timeout-lettura-secondi}") int timeoutLettura) {
		// Timeout di connessione sull'HttpClient del JDK; quello di lettura (intera risposta) sulla request factory
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(timeoutConnessione))
				.build();
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
		factory.setReadTimeout(Duration.ofSeconds(timeoutLettura));
		return RestClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.requestFactory(factory)
				.build();
	}
}
