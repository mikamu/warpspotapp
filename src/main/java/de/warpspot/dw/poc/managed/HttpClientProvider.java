package de.warpspot.dw.poc.managed;

import org.apache.http.client.HttpClient;

import de.warpspot.dw.poc.core.Registrable;

public class HttpClientProvider implements Registrable<HttpClientProvider> {

	private HttpClient client;
	
	public HttpClientProvider(final HttpClient pClient) {
		this.client = pClient;
	}
	
	public HttpClient getHttpClient() {
		return this.client;
	}
}
