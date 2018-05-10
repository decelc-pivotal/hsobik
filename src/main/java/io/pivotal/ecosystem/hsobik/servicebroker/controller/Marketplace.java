package io.pivotal.ecosystem.hsobik.servicebroker.controller;

import java.util.Date;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.organizations.ListOrganizationsRequest;
import org.cloudfoundry.client.v2.organizations.ListOrganizationsResponse;
import org.cloudfoundry.client.v2.services.ListServicesRequest;
import org.cloudfoundry.client.v2.services.ListServicesResponse;
import org.cloudfoundry.client.v2.services.ServiceEntity;
import org.cloudfoundry.client.v2.services.ServiceResource;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class Marketplace {

	@Bean
	DefaultConnectionContext connectionContext(@Value("${cf.apiHost:api.system.decelles.io}") String apiHost,
			@Value("${cf.skipSslValidation:true}") Boolean skipSslValidation) {
		return DefaultConnectionContext.builder().apiHost(apiHost).skipSslValidation(skipSslValidation).build();
	}

	@Bean
	PasswordGrantTokenProvider tokenProvider(@Value("${cf.username:admin}") String username,
			@Value("${cf.password}") String password) {

		System.out.println("tokenprovider username=" + username);
		System.out.println("tokenprovider password=" + password);

		return PasswordGrantTokenProvider.builder().password(password).username(username).build();
	}

	@Bean
	ReactorDopplerClient dopplerClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
		return ReactorDopplerClient.builder().connectionContext(connectionContext).tokenProvider(tokenProvider).build();
	}

	@Bean
	ReactorUaaClient uaaClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
		return ReactorUaaClient.builder().connectionContext(connectionContext).tokenProvider(tokenProvider).build();
	}

	@Bean
	DefaultCloudFoundryOperations cloudFoundryOperations(CloudFoundryClient cloudFoundryClient,
			DopplerClient dopplerClient, UaaClient uaaClient, @Value("${cf.organization:cjd-org}") String organization,
			@Value("${cf.space:development}") String space) {
		return DefaultCloudFoundryOperations.builder().cloudFoundryClient(cloudFoundryClient)
				.dopplerClient(dopplerClient).uaaClient(uaaClient).organization(organization).space(space).build();
	}

	@Bean
	ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
		return ReactorCloudFoundryClient.builder().connectionContext(connectionContext).tokenProvider(tokenProvider)
				.build();
	}

	@Autowired
	private CloudFoundryOperations cloudFoundryOperations;

	@Autowired
	private CloudFoundryClient cloudFoundryClient;

	@GetMapping("/test")
	public JSONObject test() {

		System.out.println("==== in test ====");

		// cloudFoundryOperations.organizations().list().map(OrganizationSummary::getName).subscribe(System.out::println);
		/// cloudFoundryOperations.buildpacks().list().map(Buildpack::getName).subscribe(System.out::println);
		// cloudFoundryOperations.applications().list().map(ApplicationSummary::getName).subscribe(System.out::println);

		// cloudFoundryOperations.serviceAdmin().list().map(ServiceBroker::getName).subscribe(System.out::println);

		// cloudFoundryOperations.spaces().list().map(SpaceSummary::getName).subscribe(System.out::println);

		cloudFoundryClient.organizations().list(ListOrganizationsRequest.builder().page(1).build())
				.flatMapIterable(ListOrganizationsResponse::getResources).map(resource -> OrganizationSummary.builder()
						.id(resource.getMetadata().getId()).name(resource.getEntity().getName()).build())
				.subscribe(System.out::println);
		//ListServicePlansResponse.

		// ListServicePlansResponse.builder().resources(elements)
		// ServicePlanResource.builder().entity(entity)
		// cloudFoundryClient.servicePlans().list(ListServicePlansRequest.builder().build())
		// .flatMapIterable(ListServicePlansResponse::getResources).map(resource
		// -> OrganizationSummary.builder()
		// .id(resource.getMetadata().getId()).name(resource.getEntity().getName()).build())
		// .subscribe(System.out::println);

		cloudFoundryClient.services().list(ListServicesRequest.builder().build())
				.flatMapIterable(ListServicesResponse::getResources).map(resource -> ServiceResource.builder()
						.entity(ServiceEntity.builder().build()).build())
						.subscribe(System.out::println);

		
		// cloudFoundryClient.services().list(ListServicesRequest.builder().build()).
				//).subscribe(System.out::println);
		// .metadata(Metadata.builder().build();
		// id(resource.getMetadata().getId()).build())
		// .subscribe(System.out::println);

		// .id(resource.getMetadata().getId()).name(resource.getEntity().getName()).build())

		// .subscribe(System.out::println);
		
		

		// .metadata(Metadata.builder()
		// .id("69b84c38-e786-4270-9cca-59d02a700798")
		/// .url("/v2/services/69b84c38-e786-4270-9cca-59d02a700798")
		// .createdAt("2015-07-27T22:43:35Z")
		// .build()).build());
		
		//				.id(resource.getMetadata().getId()).name(resource.getEntity().getName()).build())
		//		.subscribe(System.out::println);

		// (resource -> ServiceResponse.builder());

		// .flatMap(ListServicesResponse::getResources);
		
		
		
		// .flatMapIterable(ListServicesResponse::getResources).map(resource ->
		// OrganizationSummary.builder()
		// .id(resource.getMetadata().getId()).name(resource.getEntity().getName()).build())
		// .subscribe(System.out::println);

		JSONObject json = new JSONObject();
		json.put("timestamp", new Date().toString());

		// json.put("day_chg", String.valueOf(marketData.getDay_chg()));
		// json.put("month_chg", String.valueOf(marketData.getMonth_chg()));
		// json.put("year_chg", String.valueOf(marketData.getYear_chg()));
		return json;

		// return "";
		// return new Greeting(counter.incrementAndGet(),
		// String.format(template, name));
	}

	@GetMapping("/marketplace")
	public JSONObject marketplace() {
		System.out.println("==== in greeting ====");
		JSONObject json = new JSONObject();
		json.put("timestamp", new Date().toString());

		// json.put("day_chg", String.valueOf(marketData.getDay_chg()));
		// json.put("month_chg", String.valueOf(marketData.getMonth_chg()));
		// json.put("year_chg", String.valueOf(marketData.getYear_chg()));
		return json;

		// return "";
		// return new Greeting(counter.incrementAndGet(),
		// String.format(template, name));
	}

}
