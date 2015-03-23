package de.warpspot.dw.poc.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.http.client.HttpClient;

import de.warpspot.dw.poc.GoogleOIDC;
import de.warpspot.dw.poc.GoogleOIDCTokenData;
import de.warpspot.dw.poc.core.ServiceRegistry;
import de.warpspot.dw.poc.managed.CSRFTokenManager;
import de.warpspot.dw.poc.managed.HttpClientProvider;
import de.warpspot.dw.poc.views.LoginWithGoogleResultView;

@Path("/oauth2callback")
@Produces("text/html; charset=utf-8")
public class OAuthCallbackResource {

	private final GoogleOIDC googleOICD;
	
	public OAuthCallbackResource() {
		this.googleOICD = GoogleOIDC.newOIDC();
	}
	
	@GET
	public LoginWithGoogleResultView callback(@QueryParam("code") String pCode, @QueryParam("state") String pState) {
		GoogleOIDCTokenData tokenData = null;
		final boolean stateIsValid = validateState(pState);
		if (stateIsValid) {
			tokenData = this.googleOICD.retrieveTokenDataForCode(pCode, getHttpClient());
		}
		
		return new LoginWithGoogleResultView(tokenData);
	}

	private boolean validateState(final String pState) {
		return ServiceRegistry.get().getService(CSRFTokenManager.class).map(mgr -> mgr.validateToken(pState)).orElse(false);
	}
	
	private HttpClient getHttpClient() {
		return ServiceRegistry.get().getService(HttpClientProvider.class).map(prov -> prov.getHttpClient()).orElse(null);
	}
}
