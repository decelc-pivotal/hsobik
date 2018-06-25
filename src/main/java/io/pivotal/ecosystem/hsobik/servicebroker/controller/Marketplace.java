package io.pivotal.ecosystem.hsobik.servicebroker.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class Marketplace {

	public Marketplace() {

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

	}

	private static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
		}
	} };

	@Bean
	DefaultConnectionContext connectionContext(@Value("${cf.apiHost}") String apiHost,
			@Value("${cf.skipSslValidation:true}") Boolean skipSslValidation) {
		return DefaultConnectionContext.builder().apiHost(apiHost).skipSslValidation(skipSslValidation).build();
	}

	@Bean
	PasswordGrantTokenProvider tokenProvider(@Value("${cf.username}") String username,
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
			DopplerClient dopplerClient, UaaClient uaaClient,
			@Value("${cf.organization}") String organization, @Value("${cf.space}") String space) {
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
	public Mono<ListServicesResponse> test() {

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

		Mono<ListServicesResponse> s = cloudFoundryClient.services().list(ListServicesRequest.builder().build());

		// .subscribe(System.out::println);

		// .flatMapIterable(ListServicesResponse::getResources).map(resource ->
		// ServiceResource.builder()
		// .entity(ServiceEntity.builder().build()).build())
		// .subscribe(System.out::println);


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
		return s;

		// return "";
		// return new Greeting(counter.incrementAndGet(),
		// String.format(template, name));
	}

	@GetMapping("/marketplace")
	public JSONObject marketplace() {
		System.out.println("==== in greeting ====");

		cloudFoundryClient.services().list(ListServicesRequest.builder().build())
				.flatMapIterable(ListServicesResponse::getResources)
				.map(resource -> ServiceResource.builder().entity(ServiceEntity.builder().build()).build())
				.subscribe(System.out::println);

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

	@GetMapping("/serviceCatalog")
	public JsonNode k8sServiceCatalog() {

		String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6IjRhNDA5YTUyLWY0MjQtNGFiOC1iY2Q5LWVkMmFjNGM5YTllNC10b2tlbi1nOW1jNCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiI0YTQwOWE1Mi1mNDI0LTRhYjgtYmNkOS1lZDJhYzRjOWE5ZTQiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiIxYjEzYjgyYy01NDYzLTExZTgtOTIwNi00MjAxYzBhODE0MGYiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6ZGVmYXVsdDo0YTQwOWE1Mi1mNDI0LTRhYjgtYmNkOS1lZDJhYzRjOWE5ZTQifQ.n57MnxvoDIlpi5sJb9KelZdqWSEukEDVcV2nwVUyW-mbKx96cbFDsyS7S6smRo7nrPGO0CQhtk7BHjG_dc8ZH5ddHUz633GTi3TSI9QvVg_MiyGjkQ4w33V0ta1wc9ldRwypYPHerWd7r0AxizdnR9Cl39wmAhcL26JHwuEpXnSingEqZgoVOJP5tO2jV7wRNNVNo5Ic0wA_rLQV323CM7dH1VjPUnRWgqtDz0VR_gvAQtDCFJo0XGnu2kr9ToYopJzrFasBCD94FwqqT7BVaU7bYtAjjmR0bHcvDESpNNAMxIqhz86631r-001TdzLZZ9DXr54-xFCdrMbcS0KWlQ";

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", "Bearer " + token);
		System.out.println("headers = " + headers);

		RestTemplate restTemplate = new RestTemplate();
		String fooResourceUrl = "https://35.185.58.92:8443/apis/servicecatalog.k8s.io/v1beta1/clusterservicebrokers";
		// HttpEntity<> request = new HttpEntity<>(null, headers);
		ResponseEntity<String> response = restTemplate.exchange(fooResourceUrl, HttpMethod.GET,
				new HttpEntity<>(null, headers), String.class);

		// restTemplate.getfo
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = null;
		try {
			root = mapper.readTree(response.getBody());
			System.out.println("response = " + response.getBody());

			JsonNode name = root.path("name");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// assertThat(name.asText(), notNullValue());

		return root;
	}

	private String getAuthorizationHeader(String clientId, String clientSecret) {
		String creds = String.format("%s:%s", clientId, clientSecret);
		try {
			return "Basic " + new String(Base64.encode(creds.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Could not convert String");
		}
	}

}
