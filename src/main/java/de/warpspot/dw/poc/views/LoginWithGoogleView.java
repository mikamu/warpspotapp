package de.warpspot.dw.poc.views;

import de.warpspot.dw.poc.GoogleOIDCProps;
import de.warpspot.dw.poc.core.ServiceRegistry;
import de.warpspot.dw.poc.managed.CSRFTokenManager;
import io.dropwizard.views.View;

public class LoginWithGoogleView extends View {

	final GoogleOIDCProps googleOICDProps;
	
	public LoginWithGoogleView(final GoogleOIDCProps pGoogleOICDProps) {
		super("loginwithgoogle.mustache");
		this.googleOICDProps = pGoogleOICDProps;
	}
	
	public String getAuthenticationURI() {
		final String token = ServiceRegistry.get().getService(CSRFTokenManager.class).map(mgr -> mgr.generateToken()).orElse(null);
		return this.googleOICDProps.createAuthURLForCSRFToken(token);
	}
}
