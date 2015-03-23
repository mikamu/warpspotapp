package de.warpspot.dw.poc.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import de.warpspot.dw.poc.GoogleOIDC;
import de.warpspot.dw.poc.views.LoginWithGoogleView;

@Path("/loginWithGoogle")
@Produces("text/html; charset=utf-8")
public class LoginWithGoogleButtonResource {

	@GET
	public LoginWithGoogleView view() {
		return new LoginWithGoogleView(GoogleOIDC.newOIDC().properties());
	}

}
