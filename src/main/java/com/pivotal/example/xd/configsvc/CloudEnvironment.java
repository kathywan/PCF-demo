package com.pivotal.example.xd.configsvc;

import io.pivotal.spring.cloud.service.common.ConfigServerServiceInfo;
import io.pivotal.spring.cloud.service.config.ConfigClientOAuth2ResourceDetails;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.context.support.StandardServletEnvironment;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class CloudEnvironment extends StandardServletEnvironment {

    @Override
    public void initPropertySources(ServletContext servletContext, ServletConfig servletConfig) {
        super.initPropertySources(servletContext,servletConfig);
        customizePropertySources(this.getPropertySources());
    }

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        super.customizePropertySources(propertySources);
        try {
            PropertySource<?> source = initConfigServicePropertySourceLocator(this);
            propertySources.addLast(source);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private PropertySource<?> initConfigServicePropertySourceLocator(Environment environment) {

        CloudFactory cloudFactory = new CloudFactory();
        Cloud cloud = cloudFactory.getCloud();
        ConfigServerServiceInfo configServiceInfo = cloud.getSingletonServiceInfoByType(ConfigServerServiceInfo.class);

        String uriCloudConfig = configServiceInfo.getUri();
//        String appName = String.valueOf(cloud.getApplicationInstanceInfo().getProperties().get("name"));
        ConfigClientProperties configClientProperties = new ConfigClientProperties(environment);
        configClientProperties.setUri(uriCloudConfig);
//        configClientProperties.setName(appName);

        System.out.println("##################### will load the client configuration");
        System.out.println(configClientProperties);

        ConfigServicePropertySourceLocator configServicePropertySourceLocator =
                new ConfigServicePropertySourceLocator(configClientProperties);

        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(configClientOAuth2ResourceDetails(configServiceInfo));
        configServicePropertySourceLocator.setRestTemplate(oAuth2RestTemplate);

        return configServicePropertySourceLocator.locate(environment);
    }

    private ConfigClientOAuth2ResourceDetails configClientOAuth2ResourceDetails(ConfigServerServiceInfo configServiceInfo) {
        String strAccessTokenUri = configServiceInfo.getAccessTokenUri();
        String strClientId = configServiceInfo.getClientId();
        String strClientSecret = configServiceInfo.getClientSecret();

        ConfigClientOAuth2ResourceDetails resource = new ConfigClientOAuth2ResourceDetails();
        resource.setAccessTokenUri(strAccessTokenUri);
        resource.setClientId(strClientId);
        resource.setClientSecret(strClientSecret);
        return resource;
    }

}