package io.pivotal.ecosystem.hsobik;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class HsobikSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.csrf().disable().authorizeRequests().antMatchers("/v2/**").hasRole("ADMIN").antMatchers("/error/**")
				.permitAll().and().httpBasic();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public InMemoryUserDetailsManager userDetailsService() {
		return new InMemoryUserDetailsManager(adminUser());
	}

	private UserDetails adminUser() {
		// UserBuilder users= Users.withDefaultPasswordEncoder;
		// User.withDefaultPasswordEncoder().username("user").password("user").roles("USER").build();
		// User.withDefaultPasswordEncoder()

		return User.withUsername("admin").password(passwordEncoder().encode("supersecret")).roles("ADMIN").build();
	}
}
