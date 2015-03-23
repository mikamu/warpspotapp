package de.warpspot.dw.poc;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;

public class HelloWorldConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    @NotEmpty
	private String template;
	
	@NotEmpty
	private String defaultName = "Stranger";

	@Valid
	@NotNull
	private GoogleOIDCProps googleOICDProps;
	
	@JsonProperty
	public String getTemplate() {
		return template;
	}
	
	@JsonProperty
	public void setTemplate(final String pTemplate) {
		this.template = pTemplate;
	}
	
	@JsonProperty
	public String getDefaultName() {
		return defaultName;
	}
	
	@JsonProperty
	public void setDefaultName(final String pName) {
		this.defaultName = pName;
	}

    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }
    
	@JsonProperty
	public GoogleOIDCProps getGoogleOICDProps() {
		return googleOICDProps;
	}
	
	@JsonProperty
	public void setGoogleOICDProps(final GoogleOIDCProps pGoogleOICDProps) {
		this.googleOICDProps = pGoogleOICDProps;
	}
}
