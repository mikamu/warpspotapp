package de.warpspot.dw.poc.views;

import de.warpspot.dw.poc.GoogleOIDCTokenData;
import io.dropwizard.views.View;

public class LoginWithGoogleResultView extends View {

	private final GoogleOIDCTokenData tokenData;
	
	public LoginWithGoogleResultView(final GoogleOIDCTokenData pTokenData) {
		super("loginwithgoogleresultview.mustache");
		this.tokenData = pTokenData;
	}

	public GoogleOIDCTokenData getTokenData() {
		return this.tokenData;
	}
}
