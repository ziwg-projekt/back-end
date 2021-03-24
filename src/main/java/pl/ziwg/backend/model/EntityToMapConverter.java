package pl.ziwg.backend.model;


import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import org.apache.log4j.Logger;
import java.util.*;

public class EntityToMapConverter {
    protected static final Logger log = Logger.getLogger(EntityToMapConverter.class);

    public static Map<String, Object> getRepresentationWithoutChosenFields(Object object, List<String> excludedFields){
        Map<String, Object> representation = new HashMap<>();
        for(Field field : ArrayUtils.addAll(object.getClass().getDeclaredFields(), object.getClass().getFields())){
            if(!excludedFields.contains(field.getName())){
                field.setAccessible(true);
                try {
                    representation.put(field.getName(), field.get(object));
                } catch (IllegalAccessException e) {
                    log.error("Cannot access field " + field.toString() + " cause of " + e.getMessage());
                }
            }
        }
        return representation;
    }

    public static <T> List<Map<String, Object>> getListRepresentationWithoutChosenFields(Set<T> resources, List<String> excludedFields){
        List<Map<String, Object>> response = new ArrayList<>();
        for(Object a : resources){
            response.add(getRepresentationWithoutChosenFields(a, excludedFields));
        }
        return response;
    }

}
