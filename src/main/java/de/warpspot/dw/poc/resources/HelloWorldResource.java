package de.warpspot.dw.poc.resources;

import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;

import de.warpspot.dw.poc.model.Saying;


@Path("/hello-world")
@Produces("application/json; charset=utf-8")
public class HelloWorldResource {
	private final String template;
	private final String defaultName;
	private final AtomicLong counter;
	
	public HelloWorldResource(final String pTemplate, final String pDefaultName) {
		this.template = pTemplate;
		this.defaultName = pDefaultName;
		this.counter = new AtomicLong();
	}
	
	@GET
	@Timed
	public Saying sayHello(@QueryParam("name") Optional<String> pName) {
		final String value = String.format(template, pName.or(defaultName));
		return new Saying(counter.incrementAndGet(), value);
	}
}
