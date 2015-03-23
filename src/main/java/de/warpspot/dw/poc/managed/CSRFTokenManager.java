package de.warpspot.dw.poc.managed;

import java.util.Set;
import java.util.UUID;

import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.warpspot.dw.poc.core.Registrable;
import io.dropwizard.lifecycle.Managed;

public class CSRFTokenManager implements Managed, Registrable<CSRFTokenManager> {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	final Set<String> validTokens = new ConcurrentHashSet<String>();
	
	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {
		this.validTokens.clear();
	}
	
	public String generateToken() {
		final String token = UUID.randomUUID().toString();
		this.validTokens.add(token);
		return token;
	}
	
	public boolean validateToken(final String pToken) {
		if (logger.isInfoEnabled()) {
			logger.info("token = " + pToken);
			logger.info("validTokens = " + this.validTokens.toString());
		}
		if (pToken != null) {
			final boolean result = this.validTokens.remove(pToken);
			logger.info("Token is valid: " + result);
			return result;
		}
		return false;
	}
}
