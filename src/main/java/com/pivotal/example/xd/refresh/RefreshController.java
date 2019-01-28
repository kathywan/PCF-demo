package com.pivotal.example.xd.refresh;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.support.XmlWebApplicationContext;

@Controller
public class RefreshController implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Autowired
    private RefreshScope refreshScope;

    @RequestMapping(path = "/refreshall", method = RequestMethod.GET)
    public @ResponseBody String refresh() {
//        refreshScope.refreshAll();
//        refreshctx();
        ContextRefresher contextRefresher = new ContextRefresher((ConfigurableApplicationContext)applicationContext, refreshScope);
        contextRefresher.refresh();
        return "Refreshed";
    }

    public void refreshctx(){
        ((XmlWebApplicationContext)(applicationContext)).refresh();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}