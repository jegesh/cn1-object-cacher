# cn1-object-cacher
This CodenameOne library enables apps to easily cache data downloaded from the
app server (or any other source) in the device's file system. Cached data is saved in JSON format, one file per object type.  Caching data can potentially improve
the app's performance and responsivity, cut down on cell data usage, extend battery life, and enable
'offline mode'.

## Installation
Installing the library is the same as any another cn1 library, as explained in the [docs](https://www.codenameone.com/manual/advanced-topics.html#_libraries_-_cn1lib).
You can either install this library using the cn1lib wizard, or clone the repository, build the project in your IDE,
and copy the `.cn1lib` file from the `/dist` folder.

## Usage
The data caching cycle usually looks like this:

`Initialize cache (first time only) -> Sync local cache to server -> Retrieve/modify data from cache -> Update server`

Depending on the needs of the application, the cache may need to be resynced from the server periodically (if it's an app in which users exchange data).
All the various stages of this cycle are exposed by the library's API.

### Initialization
Each `CacheFile` instance is responsible for a single class of objects that are to be cached.  For instance, an app
that allows users to trade used cars, there might be a `Car` object and a `User` object that you want to cache on the device.

```Java
public class Car {
	private long carId;
	private String model;
	private String condition;
	private int year;
	private long ownerId;
	
	...
	(getters and setters)
}

public class User {
	private long userId;
	private String name;
	private String email;
	
	...
	(getters and setters)
}
```

#### Implementing CacheSerializer
Each one of these classes needs its own `CacheFile` to manage its cached data, and a `CacheSerializer` to serialize and deserialize the objects.
In this case, implementing the serializer is quite straightforward:

```Java
public class CarSerializer implements CacheSerializer<Car>{
	public Car deserialize(JSONObject rawData){
		Car car = new Car();
		car.setId(rawData.getLong("id"));
		car.setModel(rawData.getString("model"));
		car.setCondition(rawData.getString("condition"));
		car.setYear(rawData.getInt("year"));
		car.setOwnerId(rawData.getLong("owner"));
		return car;
	};
	
    public JSONObject serialize(Car car){
		return new JSONObject()
		.put("id", car.getId())
		.put("model", car.getModel())
		.put("condition", car.getCondition())
		.put("year", car.getYear())
		.put("owner", car.getOwnerId());
	};
    
    default public Object getObjectId(Car car){
        return object.getId();
    }
}

```
__Note:__ The `getObjectId` method must return a unique value for each object in the given cache.

A similar serializer can be easily implemented for the `User` class. Once we've done that,
we can instantiate a `CacheFile` object and specify which class's cache it will manage by setting the typed parameter.

For instance, in our car trading app, we need two different instances:

```
CacheFile<Car> carCache = new CacheFile(FileSystemStorage.getInstance().getAppHomePath() + "cache/car.json", new CarSerializer(), true);
CacheFile<User> userCache = new CacheFile(FileSystemStorage.getInstance().getAppHomePath() + "cache/user.json", new UserSerializer(), true);
```

#### CacheFile Constructor
The constructor for `CacheFile` takes three arguments, as seen above.

The first argument is the absolute path to the file on the device that
will contain the cached data.  The file will be created if it doesn't exist, and will be overwritten if it does.  The containing directory must be
created ahead of time.  For instance, if you want to store my cache at `/myappdir/cache/car.json`, you will need to create the directory
`/myappdir/cache` before calling the constructor.

The second argument is a `CacheSerializer` implementation with the same typed parameter as the `CacheFile`.

The third argument is boolean, which determines whether or not the cache will be held in memory as well, or only in the filesystem (`true` for memory caching).  Memory caching provides for much quicker retrieval and manipulation of data,
and can cut down on battery usage, but can be a strain on the device's resources if very large datasets are being cached.  If memory caching is used,
the data needs to be persisted to the file via the [`CacheFile.syncFromMemCacheAsync()`](https://github.com/jegesh/cn1-object-cacher/blob/1be068eae75e272f3312ab8b6d129787f73d4df3/src/net/gesher/cn1/caching/CacheFile.java#L188) method
before exiting the app, or all changes will be lost.

It is best practice to use only a single instance of `CacheFile` per type of object being stored.  One simple way to achieve this is by
instantiated all `CacheFile`s in the cn1 application class's `start()` method, and hold a static reference to the `CacheFile`:

```Java
public class MyApplication{
	public static CacheFile carCache<Car>;
	
	public void start(){
		carCache = new CacheFile(FileSystemStorage.getInstance().getAppHomePath() + "cache/car.json", new CarSerializer(), true);
	}
	...
}
```

And then, in the form that displays the car data:<br/>
```Java
CacheFile<Car> carCache = MyApplication.carCache;
Car[] cars = carCache.getAll(); // retrieve all cars in the cache
```

### Syncing From External Data Source

### Retrieving Lists of Objects

#### List Sorting

### Updating Objects

### Cache Validation
