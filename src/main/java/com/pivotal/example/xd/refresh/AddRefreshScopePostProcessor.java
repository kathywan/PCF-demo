package com.pivotal.example.xd.refresh;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class AddRefreshScopePostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Autowired
    private RefreshScope refreshScope;

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if(beanFactory.getRegisteredScope("refresh") == null)
            beanFactory.registerScope("refresh", refreshScope);

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
            beanDef.setLazyInit(true);
            beanDef.setScope("refresh");
        }
    }

    public void setApplicationContext(ApplicationContext context)
            throws BeansException {
        applicationContext = context;
    }

}