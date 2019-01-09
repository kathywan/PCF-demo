package com.pivotal.example.xd.configsvc;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class AddRefreshScopeProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for(int i=0; i<beanNames.length; i++){
            BeanDefinition beanDef = beanFactory.getBeanDefinition(beanNames[i]);
            beanDef.setLazyInit(true);
            beanDef.setScope("refresh");
        }
    }

    public void setApplicationContext(ApplicationContext context)
            throws BeansException {
        applicationContext = context;
    }

    /**
     * Get a Spring bean by type.
     *
     * @param beanClass
     * @return
     */
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    /**
     * Get a Spring bean by name.
     *
     * @param beanName
     * @return
     */
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }
}