package net.gesher.cn1.caching;


/**
 * 
 *  @author yaakov
 */
public final class CachingPolicy extends Enum {

	public static final CachingPolicy SYNC_ON_APP_OPEN;

	public static final CachingPolicy SYNC_HOURLY;

	public static final CachingPolicy SYNC_DAILY;

	public static CachingPolicy[] values() {
	}

	public static CachingPolicy valueOf(String name) {
	}
}
