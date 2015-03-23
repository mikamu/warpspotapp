package de.warpspot.dw.poc;

import java.io.IOException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleOIDCTokenData {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private JsonNode rawTokenData;
	
	private String bearerToken;
	private int expiresIn;
	private String rawIdToken;
	private JsonNode rawHeaderData;
	private JsonNode rawPayloadData;
	private String rawSignatureData;

	public GoogleOIDCTokenData(final JsonNode pRawTokenData) {
		this.rawTokenData = pRawTokenData;
		
		this.bearerToken = pRawTokenData.get("access_token").asText();
		this.expiresIn = pRawTokenData.get("expires_in").asInt();
		
		if (logger.isInfoEnabled()) {
			logger.info("BearerToken {} expires in {} seconds.", this.bearerToken, this.expiresIn);
		}
		
		this.rawIdToken = pRawTokenData.get("id_token").asText();
		decodeIdToken();
	}

	private void decodeIdToken() {
		if (this.rawIdToken == null) {
			throw new IllegalArgumentException("Tokendaten enthalten kein IdToken!");
		}

		final String[] idTokenParts = this.rawIdToken.split("\\.");
		logger.info("idTokenParts.length = " + idTokenParts.length);
		if (idTokenParts == null || idTokenParts.length != 3) {
			throw new IllegalArgumentException("IdToken-Struktur ist nicht valide!");
		}
		
		this.rawHeaderData = decodeAndParseTokenPart(idTokenParts[0]);
		printPart("Header: ", this.rawHeaderData);
		
		this.rawPayloadData = decodeAndParseTokenPart(idTokenParts[1]);
		printPart("Payload: ", this.rawPayloadData);

		this.rawSignatureData = idTokenParts[2];
	}
	
	private JsonNode decodeAndParseTokenPart(final String pPart) {
		final byte[] partBytes = Base64.getDecoder().decode(pPart);
		//final String partStr = new String(partBytes, StandardCharsets.UTF_8);
		try {
			return new ObjectMapper().readTree(partBytes);
		} catch (IOException ioEx) {
			logger.error("Problem beim Parsen eines Tokenteils: ", ioEx);
		}
		return null;
	}
	
	private void printPart(final String pPrefix, final JsonNode pPart) {
		if (logger.isInfoEnabled()) {
			try {
				logger.info(pPrefix + new ObjectMapper().writeValueAsString(pPart));
			} catch (final JsonProcessingException jpEx) {
				logger.error("Problem bei der Ausgabe eines Token-Parts: ", jpEx);
			}
		}
	}
}
