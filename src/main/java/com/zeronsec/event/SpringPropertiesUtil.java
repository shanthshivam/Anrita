package com.zeronsec.event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SpringPropertiesUtil {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    /**
     * Get all properties with specific prefix. 
     */
    public Map<String,String> getAllPropWithPrefix(String prefix) {
        BindResult<Map<String, String>> result = Binder.get(applicationContext.getEnvironment())
                .bind(prefix, Bindable.mapOf(String.class, String.class));
        if (!result.isBound() || result.get()==null) {
            return Collections.emptyMap();
        }
        return result.get().entrySet().stream().collect(Collectors.toMap(x->prefix+"."+x.getKey(),x->x.getValue()));
    }
}
