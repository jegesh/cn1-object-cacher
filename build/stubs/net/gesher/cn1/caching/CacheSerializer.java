package net.gesher.cn1.caching;


/**
 * 
 *  @author yaakov
 *  
 *  @param <T> 
 */
public interface CacheSerializer {

	public Object deserialize(JSONObject rawData);

	public JSONObject serialize(Object object);

	public Object getObjectId(Object object);
}
