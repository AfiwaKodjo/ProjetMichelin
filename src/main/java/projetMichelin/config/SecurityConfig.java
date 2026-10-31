package projetMichelin.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean 
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception{
            http
                .csrf(csrf -> csrf.disable())
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(convertisseurDeRoles())));
                return http.build();
                

    }

    private JwtAuthenticationConverter convertisseurDeRoles(){
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::lireRoles);
        return converter;
    }


    //On ouvre les blocs un par un pour arriver à la liste
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> lireRoles(Jwt jwt) {
        List<GrantedAuthority> roles = new ArrayList<>();

        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        //c'est pour un token sans rôle
        if (resourceAccess == null) {
            return roles; 
        }

        Map<String, Object> client = (Map<String, Object>) resourceAccess.get("coursm2");
        if (client == null) {
            return roles; 
        }

        List<String> listeRoles = (List<String>) client.get("roles");
        if (listeRoles == null) {
            return roles;
        }

        //Spring veut ROLE_USER et pas USER seulement
        for (String role : listeRoles) {
            roles.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return roles;
    }

}
