package com.pivotal.example.xd;

import io.pivotal.spring.cloud.service.common.ConfigServerServiceInfo;
import io.pivotal.spring.cloud.service.config.ConfigClientOAuth2ResourceDetails;
import io.pivotal.spring.cloud.service.config.PlainTextConfigClient;
import io.pivotal.spring.cloud.service.config.PlainTextConfigClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties({MyPropeties.class})
public class PropertiesConfigurer {

    CloudFactory cloudFactory = new CloudFactory();
    Cloud cloud = cloudFactory.getCloud();

    ConfigServerServiceInfo configServiceInfo = cloud.getSingletonServiceInfoByType(ConfigServerServiceInfo.class);
    String uriCloudConfig = configServiceInfo.getUri();
    String strAccessTokenUri = configServiceInfo.getAccessTokenUri();
    String strClientId = configServiceInfo.getClientId();
    String strClientSecret = configServiceInfo.getClientSecret();

    @Bean
    public ConfigClientOAuth2ResourceDetails resource() {

        ConfigClientOAuth2ResourceDetails resource = new ConfigClientOAuth2ResourceDetails();
        resource.setAccessTokenUri(strAccessTokenUri);
        resource.setClientId(strClientId);
        resource.setClientSecret(strClientSecret);
        return resource;
    }

    @Bean
    public ConfigClientProperties configClientProperties(Environment env) {

        String name = String.valueOf(cloud.getApplicationInstanceInfo().getProperties().get("name"));
        ConfigClientProperties configClientProperties = new ConfigClientProperties(env);
        configClientProperties.setName(name);
        configClientProperties.setUri(uriCloudConfig);
        return configClientProperties;
    }

    @Bean
    public PlainTextConfigClient configClient(ConfigClientOAuth2ResourceDetails resource, ConfigClientProperties configClientProperties) {

        PlainTextConfigClient configClient = new PlainTextConfigClientAutoConfiguration().plainTextConfigClient(resource, configClientProperties);

        return configClient;
    }

    @Bean(name = "demoConfigFileLoader")
//    @Profile({"test"})
    public Properties acsTestconfigFileLoader(Environment env, PlainTextConfigClient configClient) throws IOException {

        Resource resource = configClient.getConfigFile("test", "master", "pcfdemo.properties");
        InputStream inputStream = resource.getInputStream();
        Properties application = new Properties();
        application.load(inputStream);

        Resource sqlhostsResource = configClient.getConfigFile(application.getProperty("sqlhosts.profile"), application.getProperty("sqlhosts.branch"), application.getProperty("sqlhosts.name"));
        InputStream sqlhostsInputStream = sqlhostsResource.getInputStream();
        Files.copy(sqlhostsInputStream, Paths.get(application.getProperty("sqlhosts.name")), StandardCopyOption.REPLACE_EXISTING);

        return application;
    }

//    @Bean(name = "demoProdConfigFileLoader")
//    @Profile({"prod"})
//    public Properties acsProdconfigFileLoader(Environment env, PlainTextConfigClient configClient) throws IOException {
//
//        Resource resource = configClient.getConfigFile("prod", "master", "pcfdemo.properties");
//        InputStream inputStream = resource.getInputStream();
//        Properties application = new Properties();
//        application.load(inputStream);
//
//        Resource sqlhostsResource = configClient.getConfigFile(application.getProperty("sqlhosts.profile"), application.getProperty("sqlhosts.branch"), application.getProperty("sqlhosts.name"));
//        InputStream sqlhostsInputStream = sqlhostsResource.getInputStream();
//        Files.copy(sqlhostsInputStream, Paths.get(application.getProperty("sqlhosts.name")), StandardCopyOption.REPLACE_EXISTING);
//
//        return application;
//    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.
    }
}