/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gesher.cn1.caching;

import ca.weblite.codename1.json.JSONObject;

/**
 *
 * @author yaakov
 * 
 * @param <T> 
 */
public interface CacheSerializer<T> {
    
    public T deserialize(JSONObject rawData);
    public JSONObject serialize(T object);
    
    default public Object getObjectId(T object){
        return object.hashCode();
    }
    
}
