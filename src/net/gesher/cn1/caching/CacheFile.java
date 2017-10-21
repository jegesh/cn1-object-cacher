/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gesher.cn1.caching;

import ca.weblite.codename1.json.JSONArray;
import ca.weblite.codename1.json.JSONException;
import com.codename1.io.File;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.Preferences;
import com.codename1.io.Util;
import com.codename1.ui.Display;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 *
 * @author yaakov
 * 
 * @param <T> Type of objects held in this file
 */
public class CacheFile<T> {
    private File localCacheFile;
    private CacheSerializer<T> serializer;
    private Map<Object, T> cacheMap;
//    private static CacheFile instance;
    private Set<CachingPolicy> policies;
    private final String PREFERENCES_LAST_CACHE_SYNC;
    private long lastSyncTime;
    private boolean hasSynced = false;
    private Comparator comparator;
    
    /**
     * 
     * @param filepath absolute path to local cache file
     * @param serializer 
     * @throws IOException if unable to access file
     */
    public CacheFile(String filepath, CacheSerializer<T> serializer, boolean isUsingMemoryCache, Comparator comparator) throws IOException, JSONException{
        this.localCacheFile = new File(filepath);
        this.serializer = serializer;
        this.comparator = comparator;
        if(!localCacheFile.exists()){
            localCacheFile.createNewFile();
        }
        PREFERENCES_LAST_CACHE_SYNC = this.localCacheFile.getName() + "_cache_last_sync";
        lastSyncTime = Preferences.get(PREFERENCES_LAST_CACHE_SYNC, 0L);
        
        // load objects from file
        if(isUsingMemoryCache){
            if(comparator != null)
                cacheMap = new ValueTreeMap<>(comparator);
            else cacheMap = new TreeMap<>();
            for(T t: getDiskCache()){
                cacheMap.put(serializer.getObjectId(t), t);
            }
        }
    }
    
    public CacheFile(String filepath, CacheSerializer<T> serializer, boolean isUsingMemoryCache) throws IOException, JSONException{
        this(filepath, serializer, isUsingMemoryCache, null);
    }
    
//    public static void initialize(String filepath, CacheSerializer serializer, boolean isUsingMemoryCache) throws IOException, JSONException{
//        instance = new CacheFile(filepath, serializer, isUsingMemoryCache);
//    }
    
//    public static CacheFile getInstance(Class klass){
//        return instance;
//    }
    
    /**
     * Sets caching policies for invalidating the cache
     * @param policySet 
     */
    public void setPolicy(Set<CachingPolicy> policySet){
        this.policies = policySet;
    }
    
    /**
     * @return true if cache is still valid according to policies that were set. If no policies were set, will always return true
     */
    public boolean isCacheValid(){
        for(CachingPolicy policy: policies){
            if(!checkPolicy(policy)) return false;
        }
        return true;
    }
    
    private boolean checkPolicy(CachingPolicy policy){
        if(policy.equals(CachingPolicy.SYNC_ON_APP_OPEN)){
            return hasSynced;
        }
        if(policy.equals(CachingPolicy.SYNC_HOURLY)){
            return new Date().getTime() - lastSyncTime >= 1000*60*60;
        }
        if(policy.equals(CachingPolicy.SYNC_DAILY))
            return new Date().getTime() - lastSyncTime >= 1000*60*60*24;
        throw new IllegalArgumentException("Unknown caching policy: " + policy.name());
    }
    
    /**
     * overwrites cache with fresh data from external source
     * @param data
     * @param notifier used to receive exceptions from the syncing process
     * @throws IOException 
     */
    public void syncAll(List<T> data, CacheErrorNotifier notifier) throws IOException{
        if(cacheMap != null){
            cacheMap.clear();
        
            for(int i = 0;i < data.size();i++){
                if(cacheMap != null)
                    cacheMap.put(serializer.getObjectId(data.get(i)), data.get(i));
            }
        }
        
        // write to file
        syncFromMemCacheAsync(data, notifier);
        Preferences.set(PREFERENCES_LAST_CACHE_SYNC, new Date().getTime());
        hasSynced = true;
    }
    
    /**
     * 
     */
    private ArrayList<T> getDiskCache() throws IOException, JSONException{
        ArrayList<T> cacheArray = new ArrayList<>();
        
        InputStream fileStream = FileSystemStorage.getInstance().openInputStream(localCacheFile.getAbsolutePath());
        String fileContents = Util.readToString(fileStream);
        if(fileContents.length() > 1){
            JSONArray cacheJson = new JSONArray(fileContents);
            Log.p(this.getClass().getSimpleName() + ": Read " + cacheJson.length() + " entries from JSON cache", Log.INFO);

            for(int i = 0;i < cacheJson.length();i++){
                T obj = serializer.deserialize(cacheJson.getJSONObject(i));
                cacheArray.add(obj);
            }
        }else{
            Log.p(this.getClass().getSimpleName() + ": JSON cache empty", Log.INFO);
        }
        return cacheArray;
    }
    
    /**
     * 
     * @param obj Object to update 
     * @param notifier ignored if memory caching is used
     * @throws java.io.IOException if file doesn't exist
     * @throws ca.weblite.codename1.json.JSONException if the cache file has been corrupted 
     */
    public void update(T obj, CacheErrorNotifier notifier) throws IOException, JSONException {
        if(cacheMap != null){
            cacheMap.put(serializer.getObjectId(obj), obj);
            return;
        }

        ArrayList<T> all = getDiskCache();
        for(int i = 0;i < all.size();i++){
            Object o = all.get(i);
            if(serializer.getObjectId((T)o).equals(serializer.getObjectId(obj))){
                all.add(i, obj);
                syncAll(all, notifier);
                return;
            }
        }
        Log.p(this.getClass().getSimpleName() + ": Attempted object update failed - "
                + "object with id " + serializer.getObjectId(obj) + " not found", Log.WARNING);
    }
    
    /**
     * syncs file from fresh data, on worker thread
     * @param updatedData
     * @param notifier 
     */
    public void syncFromMemCacheAsync(List<T> updatedData, CacheErrorNotifier notifier){
        // update file
        Display.getInstance().scheduleBackgroundTask(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray dataArray = new JSONArray();
                    for(T t: updatedData)
                        dataArray.put(serializer.serialize(t));

                    if (!localCacheFile.exists()) {
                        localCacheFile.createNewFile();
                    }
                    try (OutputStream outputStream = FileSystemStorage.getInstance().openOutputStream(localCacheFile.getAbsolutePath())) {
                        outputStream.write(dataArray.toString().getBytes());
                        outputStream.flush();
                    }
                    Log.p(this.getClass().getSimpleName() + ": Synched " + dataArray.length() + " entries to cache file " + localCacheFile.getName(), Log.INFO);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    if (notifier != null) {
                        notifier.notify(ex);
                    }
                }
            }
        });
    }

    public T get(Object id) throws IOException, JSONException {
        if (cacheMap != null) {
            return cacheMap.get(id);
        }

        for (Object o: getDiskCache()){
            T t = (T)o;
            if(serializer.getObjectId(t).equals(o)) return t;
        }
        return null;
    }
    
    /**
     * 
     * @param obj Object to remove 
     * @param notifier ignored if memory caching is being used
     * @return indicates whether or not object was found
     */
    public boolean remove(T obj, CacheErrorNotifier notifier) throws IOException, JSONException{
        if(cacheMap != null){
            if(cacheMap.containsKey(serializer.getObjectId(obj))){
                cacheMap.remove(serializer.serialize(obj));
                return true;
            }else return false;
        }
        ArrayList<T> objs = getDiskCache();
        for(int i = 0;i < objs.size();i++){
            Object o = objs.get(i);
            if(serializer.getObjectId((T)o).equals(serializer.getObjectId(obj))){
                objs.remove(i);
                syncAll(objs, notifier);
                return true;
            }
        }
        return false;
    }
    
    private T[] copyRange(T[] origin, int start, int finish){
        T[] rangedArray = (T[])new Object[finish-start+1];
        for(int i = 0;i < finish-start+1;i++){
            rangedArray[i] = origin[i + start];
        }
        return rangedArray;
    }
    
    /**
     * 
     * @param start 0-index position of first object to return
     * @param finish 0-index position of last object to return
     * @return 
     */
    public List<T> getRange(int start, int finish) throws IOException, JSONException{
        if(cacheMap != null){
            return (new ArrayList<>(cacheMap.values()).subList(start, finish+1));
        }
            
        return getCachedObjectsSorted().subList(start, finish + 1);
    }
    
    public List<T> getAll() throws IOException, JSONException{
        return getCachedObjectsSorted();
    }
    
    public int size() throws IOException, JSONException{
        if(cacheMap == null)
            return getDiskCache().size();
        return cacheMap.size();
    }
    
    private ArrayList<T> toArrayList(Object[] array){
        ArrayList<T> aList = new ArrayList<>(array.length);
        for(Object o: array)
            aList.add((T) o);
        return aList;
    }
    
    private ArrayList<T> getCachedObjectsSorted() throws IOException, JSONException{
        if(cacheMap != null){
            return new ArrayList<>(cacheMap.values());
        }
        Object[] objArray = getDiskCache().toArray();
        Arrays.sort(objArray, comparator);
        return toArrayList(objArray);
    }
    
    public boolean isCached(T object) throws IOException, JSONException{
        return get(serializer.getObjectId(object)) != null;
    }
    
    /**
     * 
     * @param object
     * @param notifier ignored if memory caching is being used
     * @throws IOException
     * @throws JSONException 
     */
    public void addToCache(T object, CacheErrorNotifier notifier) throws IOException, JSONException{
        if(cacheMap != null){
            cacheMap.put(serializer.getObjectId(object), object);
            return;
        }
        
        ArrayList<T> original = getDiskCache();
        original.add(object);
        syncAll(original, notifier);
    }
    
    /**
     * Interface for receiving caching related exception notifications from background thread
     */
    public static interface CacheErrorNotifier{
        void notify(Exception ex);
    }
    
}
