package com.pivotal.example.xd.configsvc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RefreshScope
public class MyPropeties {

    @Value("${greeting.name}")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @PostConstruct
    public void loadedProperties() {
        System.out.println("My Properties are: " + name);
    }
}