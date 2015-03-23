package de.warpspot.dw.poc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleOIDC {

	private static GoogleOIDCProps _globalProps = null;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final GoogleOIDCProps props;
	private GoogleOIDCTokenData tokenData;
	
	public static void init(final GoogleOIDCProps pProps) {
		GoogleOIDC._globalProps = pProps;
	}
	
	public static GoogleOIDC newOIDC() {
		return new GoogleOIDC(_globalProps);
	}
	
	private GoogleOIDC(final GoogleOIDCProps pProps) {
		this.props = pProps;
	}
	
	public GoogleOIDCProps properties() {
		return this.props;
	}
	
	public String createAuthURLForCSRFToken(final String pCsrfToken) {
		final String scopesStr = this.props.getScopes().stream().collect(Collectors.joining("%20"));
		
		final StringBuilder sb = new StringBuilder();
		sb.append(this.props.getAuthEndpoint())
			.append("?scope=").append(scopesStr)
			.append("&client_id=").append(this.props.getClientId())
			.append("&response_type=code&")
			.append("redirect_uri=").append(this.props.getRedirectUri());
		if (StringUtils.isNotEmpty(pCsrfToken)) {
			sb.append("&state=").append(pCsrfToken);		
		}
		return sb.toString();
	}
	
	public GoogleOIDCTokenData retrieveTokenDataForCode(final String pCode, final HttpClient pHttpClient) {
		final HttpPost httpPost = new HttpPost(this.props.getTokenEndpoint());
		final List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("code", pCode));
		nvps.add(new BasicNameValuePair("client_id", this.props.getClientId()));
		nvps.add(new BasicNameValuePair("client_secret", this.props.getClientSecret()));
		nvps.add(new BasicNameValuePair("redirect_uri", this.props.getRedirectUri()));
		nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		} catch (final UnsupportedEncodingException ueex) {
			logger.error("Problem beim Encoden der Anfrage an den Token-Endpoint: ", ueex);
		}
		CloseableHttpResponse resp = null;
		try {
			resp = (CloseableHttpResponse) pHttpClient.execute(httpPost);
			logger.info("statusline: " + resp.getStatusLine());
			HttpEntity respBody = resp.getEntity();
			final JsonNode tokenData = new ObjectMapper().readTree(respBody.getContent());
			EntityUtils.consume(respBody);
			this.tokenData = new GoogleOIDCTokenData(tokenData);
			return this.tokenData;
		} catch (ClientProtocolException cpEx) {
			logger.error("Problem bei der Anfrage an den Token-Endpoint: ", cpEx);
		} catch (IOException ioEx) {
			logger.error("Problem bei der Anfrage an den Token-Endpoint: ", ioEx);
		} finally {
			if (resp != null) {
				try {
					resp.close();
				} catch (IOException ioEx) {
					logger.error("Problem beim Schlieﬂen der Response: ", ioEx);
				}
			}
		}
		return null;
	}
}
