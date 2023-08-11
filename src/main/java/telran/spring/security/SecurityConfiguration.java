package telran.spring.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j

public class SecurityConfiguration {

@Bean
SecurityFilterChain congfigure(HttpSecurity httpSec) throws Exception {
	
	return httpSec.csrf(custom -> custom.disable())
	.cors(custom -> custom.disable()).authorizeHttpRequests(custom -> 
	custom.requestMatchers(HttpMethod.GET).authenticated().anyRequest().hasRole("ADMIN"))
	.sessionManagement(custom -> custom.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
	.httpBasic(Customizer.withDefaults()).build();
}



}
