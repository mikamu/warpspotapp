package de.warpspot.dw.poc;

import com.google.common.collect.ImmutableMap;

import de.warpspot.dw.poc.core.Registrable;
import de.warpspot.dw.poc.core.ServiceRegistry;
import de.warpspot.dw.poc.managed.CSRFTokenManager;
import de.warpspot.dw.poc.managed.HttpClientProvider;
import de.warpspot.dw.poc.resources.HelloWorldResource;
import de.warpspot.dw.poc.resources.LoginWithGoogleButtonResource;
import de.warpspot.dw.poc.resources.OAuthCallbackResource;
import io.dropwizard.Application;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

	public static void main(final String[] pArgs) throws Exception {
		new HelloWorldApplication().run(pArgs);
	}

	@Override
	public String getName() {
		return "Warpspot";
	}
	
	@Override
	public void initialize(Bootstrap<HelloWorldConfiguration> pBootstrap) {
        // Enable variable substitution with environment variables
        pBootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        pBootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
		
        pBootstrap.addBundle(new ViewBundle<HelloWorldConfiguration>() {
            @Override
            public ImmutableMap<String, ImmutableMap<String, String>> getViewConfiguration(HelloWorldConfiguration configuration) {
                ImmutableMap.Builder<String, ImmutableMap<String, String>> builder = ImmutableMap.builder();
                builder.put(".mustache", ImmutableMap.<String,String>of());
                return builder.build();
            }
        });
	}

	@Override
	public void run(HelloWorldConfiguration pConfig, Environment pEnv)
			throws Exception {
		
		/* ********************
		 *    Managed Objects
		 **********************/
		registerManaged(pEnv, new CSRFTokenManager());
		ServiceRegistry.get().addService(new HttpClientProvider(new HttpClientBuilder(pEnv).using(pConfig.getHttpClientConfiguration()).build(null)));
		GoogleOIDC.init(pConfig.getGoogleOICDProps());

		/* ******************
		 *    Resources
		 ********************/
		final HelloWorldResource res = new HelloWorldResource(
				pConfig.getTemplate(),
				pConfig.getDefaultName()
			);
		pEnv.jersey().register(res);
		pEnv.jersey().register(new LoginWithGoogleButtonResource());
		pEnv.jersey().register(new OAuthCallbackResource());
	}
	
	private void registerManaged(final Environment pEnv, final Managed pManaged) {
		pEnv.lifecycle().manage(pManaged);
		if (pManaged instanceof Registrable<?>) {
			ServiceRegistry.get().addService((Registrable<?>) pManaged);
		}
	}
}
