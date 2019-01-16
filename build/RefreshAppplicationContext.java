package com.pivotal.example.xd.refresh;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.XmlWebApplicationContext;

@Component
public class RefreshAppplicationContext implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    public void refreshctx(){
        ((XmlWebApplicationContext)(applicationContext)).refresh();
    }
}