package net.gesher.cn1.caching;


/**
 * 
 *  @author yaakov
 *  
 *  @param <T> Type of objects held in this file
 */
public class CacheFile {

	public static void initialize(String filepath, CacheSerializer serializer, boolean isUsingMemoryCache) {
	}

	public static CacheFile getInstance(Class klass) {
	}

	/**
	 *  Sets caching policies for invalidating the cache
	 *  @param policySet 
	 */
	public void setPolicy(java.util.Set policySet) {
	}

	/**
	 *  @return true if cache is still valid according to policies that were set. If no policies were set, will always return true
	 */
	public boolean isCacheValid() {
	}

	/**
	 *  overwrites cache with fresh data from external source
	 *  @param data
	 *  @param notifier used to receive exceptions from the syncing process
	 *  @throws IOException 
	 */
	public void syncAll(Object[] data, CacheFile.CacheErrorNotifier notifier) {
	}

	/**
	 *  
	 *  @param obj Object to update 
	 *  @param notifier ignored if memory caching is used
	 *  @throws java.io.IOException if file doesn't exist
	 *  @throws ca.weblite.codename1.json.JSONException if the cache file has been corrupted 
	 */
	public void update(Object obj, CacheFile.CacheErrorNotifier notifier) {
	}

	/**
	 *  syncs file from fresh data, on worker thread
	 *  @param updatedData
	 *  @param notifier 
	 */
	public void syncFromMemCacheAsync(Object[] updatedData, CacheFile.CacheErrorNotifier notifier) {
	}

	public Object get(Object id) {
	}

	/**
	 *  
	 *  @param obj Object to remove 
	 *  @param notifier ignored if memory caching is being used
	 *  @return indicates whether or not object was found
	 */
	public boolean remove(Object obj, CacheFile.CacheErrorNotifier notifier) {
	}

	/**
	 *  
	 *  @param start 0-index position of first object to return
	 *  @param finish 0-index position of last object to return
	 *  @return 
	 */
	public Object[] getRange(int start, int finish) {
	}

	/**
	 *  Override this method to order the objects in the map
	 *  @return 
	 */
	protected java.util.Comparator getDefaultComparator() {
	}

	/**
	 *  
	 *  @param object
	 *  @param notifier ignored if memory caching is being used
	 *  @throws IOException
	 *  @throws JSONException 
	 */
	public void addToCache(Object object, CacheFile.CacheErrorNotifier notifier) {
	}

	/**
	 *  Interface for receiving caching related exception notifications from background thread
	 */
	public static interface class CacheErrorNotifier {


		public void notify(Exception ex) {
		}
	}
}
