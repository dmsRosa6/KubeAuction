package scc.cache;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisCache {
	private static final String RedisHostname = System.getenv("REDIS_HOSTNAME");
	private static final String RedisKey = System.getenv("REDIS_KEY");
	public static final String CACHE_AUCTION_PREFIX = "auction:";
	public static final String CACHE_BID_PREFIX = "bid:";
	public static final String CACHE_QUESTION_PREFIX = "question:";
	public static final String CACHE_USER_PREFIX = "user:";
	public static final String CACHE_SESSION_PREFIX = "session:";
	public static final boolean USE_CACHE = true;
	public static final int DEFAULT_CACHE_TIMEOUT = 3600;


	private static JedisPool instance;
	
	public synchronized static JedisPool getCachePool() {
		if( instance != null)
			return instance;
		final JedisPoolConfig poolConfig = new JedisPoolConfig();

		poolConfig.setMaxTotal(128);
		poolConfig.setMaxIdle(128);
		poolConfig.setMinIdle(16);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		poolConfig.setNumTestsPerEvictionRun(3);
		poolConfig.setBlockWhenExhausted(true);

		if (RedisKey == null) {
			instance = new JedisPool(poolConfig, RedisHostname, 6379, 1000, false);
		}
		else
			instance = new JedisPool(poolConfig, RedisHostname, 6380, 1000, RedisKey, true);

		return instance;
		
	}
}
