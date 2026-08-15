package com.futbolin.core.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    @Bean
    HibernatePropertiesCustomizer namingCustomizer() {
        return props -> props.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, new CamelCaseToUnderscoresNamingStrategy());
    }
}
