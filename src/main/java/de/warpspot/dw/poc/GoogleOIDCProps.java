package de.warpspot.dw.poc;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

public class GoogleOIDCProps {
	
	private static final String KEY_AUTH_ENDPOINT = "authorization_endpoint";
	private static final String KEY_TOKEN_ENDPOINT = "token_endpoint";
	private static final String KEY_USERINFO_ENDPOINT = "userinfo_endpoint";
	private static final String KEY_REVOCATION_ENDPOINT = "revocation_endpoint";
	private static final String KEY_JWKS_URI = "jwks_uri";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final GoogleOpenIdConfigProvider openidConfigProvider = new GoogleOpenIdConfigProvider();
	
	@NotEmpty
	private String clientId;
	
	@NotEmpty
	private String clientSecret;

	@NotEmpty
	private String redirectUri;
	
	@Valid
	@NotNull
	private List<String> scopes;
	
	private JsonNode googleOpenIdConfig = null;
	
	public GoogleOIDCProps() {
		CompletableFuture.supplyAsync(this.openidConfigProvider::readConfig)
			.thenAccept(this::initGoogleOpenIdConfig);
	}

	@JsonProperty
	public String getClientId() {
		return clientId;
	}
	
	@JsonProperty
	public void setClientId(final String pClientId) {
		this.clientId = pClientId;
	}

	@JsonProperty
	public String getClientSecret() {
		return clientSecret;
	}
	
	@JsonProperty
	public void setClientSecret(final String pClientSecret) {
		this.clientSecret = pClientSecret;
	}
	
	@JsonProperty
	public String getRedirectUri() {
		return redirectUri;
	}
	
	@JsonProperty
	public void setRedirectUri(final String pRedirectUri) {
		this.redirectUri = pRedirectUri;
	}
	
	@JsonProperty
	public List<String> getScopes() {
		return scopes;
	}

	@JsonProperty
	public void setScopes(final List<String> pScopes) {
		this.scopes = pScopes;
	}
	
	/* ****************************************************************
	 * Nicht-JSON-Methoden
	 ******************************************************************/
	
	public String getAuthEndpoint() {
		return getConfigData(KEY_AUTH_ENDPOINT);
	}
	
	public String getTokenEndpoint() {
		return getConfigData(KEY_TOKEN_ENDPOINT);
	}
	
	public String getUserinfoEndpoint() {
		return getConfigData(KEY_USERINFO_ENDPOINT);
	}

	public String getRevocationEndpoint() {
		return getConfigData(KEY_REVOCATION_ENDPOINT);
	}

	public String getJwksUri() {
		return getConfigData(KEY_JWKS_URI);
	}

	public String createAuthURLForCSRFToken(final String pCsrfToken) {
		final String scopesStr = getScopes().stream().collect(Collectors.joining("%20"));
		
		final StringBuilder sb = new StringBuilder();
		sb.append(getAuthEndpoint())
			.append("?scope=").append(scopesStr)
			.append("&client_id=").append(getClientId())
			.append("&response_type=code&")
			.append("redirect_uri=").append(getRedirectUri());
		if (StringUtils.isNotEmpty(pCsrfToken)) {
			sb.append("&state=").append(pCsrfToken);		
		}
		return sb.toString();
	}
	
	private String getConfigData(final String pKey) {
		Preconditions.checkNotNull(this.googleOpenIdConfig);
		return this.googleOpenIdConfig.get(pKey).asText();
	}
	
	private void initGoogleOpenIdConfig(final JsonNode pConfig) {
		if (pConfig != null) {
			this.googleOpenIdConfig = pConfig;
			try {
				logger.info("google openid config: " + new ObjectMapper().writeValueAsString(pConfig));
			} catch (JsonProcessingException jsonEx) {
				logger.error("Problem beim Ausgeben der Google-OpenId-Konfig: ", jsonEx);
			}
		}
	}
	
	private final class GoogleOpenIdConfigProvider {
		private static final String GOOGLE_OPENID_CONFIG_URL = "https://accounts.google.com/.well-known/openid-configuration";

		private final Logger logger = LoggerFactory.getLogger(getClass());
		
		public JsonNode readConfig() {
			try {
				return new ObjectMapper().readTree(new URL(GOOGLE_OPENID_CONFIG_URL));
			} catch (IOException ioEx) {
				logger.error("Konnte Google OpenId-Konfiguration nicht lesen...!", ioEx);
			}
			return null;
		}
	}
}
