package com.pivotal.example.xd.configsvc;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

@Component
@RefreshScope
public class MyPropeties {

//    @Autowired
//    DataSource dataSource;

    @Value("${greeting.name}")
    private String name;

    /*public String getDbUrl() throws InvocationTargetException, IllegalAccessException {

        StringBuilder sb = new StringBuilder();

        if (dataSource == null) {
            sb.append("NULL");
        } else {
            try {
                Field urlField = ReflectionUtils.findField(dataSource.getClass(), "url");
//                ReflectionUtils.makeAccessible(urlField);
                sb.append(urlField.get(dataSource));
                sb.append(":UP");
            } catch (Exception fe) {
                try {
                    Method urlMethod = ReflectionUtils.findMethod(dataSource.getClass(), "getUrl");
//                    ReflectionUtils.makeAccessible(urlMethod);
                    sb.append(urlMethod.invoke(dataSource, (Object[]) null));
                    sb.append(":UP");
                } catch (Exception me) {
                    throw me;
                }
            }
        }
        return sb.toString();
    }*/

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