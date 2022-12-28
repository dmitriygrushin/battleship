package com.dmitriyg.battleship.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {

}
