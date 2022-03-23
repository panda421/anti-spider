package cn.edu.zjut.weining.anti.rule;

import cn.edu.zjut.weining.anti.config.AntiSpiderProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author panda421
 * @since 2021/7/8
 */
public class IpRule extends AbstractRule {



    @Override
    @SuppressWarnings("unchecked")
    protected boolean doExecute(HttpServletRequest request, HttpServletResponse response) {
        String ipAddress = getIpAddr(request);
        List<String> ignoreIpList = properties.getIpRule().getIgnoreIp();
        if (ignoreIpList != null && ignoreIpList.size() > 0) {
            for (String ignoreIp : ignoreIpList) {
                if (ignoreIp.endsWith("*")) {
                    ignoreIp = ignoreIp.substring(0, ignoreIp.length() - 1);
                }
                if (ipAddress.startsWith(ignoreIp)) {
                    return false;
                }
            }
        }
        String requestUrl = request.getRequestURI();
        //毫秒，默认5000
        int expirationTime = properties.getIpRule().getExpirationTime();
        //最高expirationTime时间内请求数
        int requestMaxSize = properties.getIpRule().getRequestMaxSize();
        // 获取请求统计数
        // RAtomicLong rRequestCount = redissonClient.getAtomicLong(RATELIMITER_COUNT_PREFIX.concat(requestUrl).concat(ipAddress));
        BoundValueOperations<String,Long> rRequestCount = redisTemplate.boundValueOps(RATELIMITER_COUNT_PREFIX.concat(requestUrl).concat(ipAddress));
        // 获取过期时间

        // RAtomicLong rExpirationTime = redissonClient.getAtomicLong(RATELIMITER_EXPIRATIONTIME_PREFIX.concat(requestUrl).concat(ipAddress));
        BoundValueOperations<String,Long> rExpirationTime = redisTemplate.boundValueOps(RATELIMITER_EXPIRATIONTIME_PREFIX.concat(requestUrl).concat(ipAddress));
        // 当前ip不存在则初始化该ip记录
        if (!redisTemplate.hasKey(RATELIMITER_EXPIRATIONTIME_PREFIX.concat(requestUrl).concat(ipAddress))) {
            rRequestCount.set(0L);
            rExpirationTime.set(0L,expirationTime, TimeUnit.MILLISECONDS);
//            rExpirationTime.expire(expirationTime, TimeUnit.MILLISECONDS);
        } else {
            BoundHashOperations<String,String,String> rHitMap = redisTemplate.boundHashOps(RATELIMITER_HIT_CRAWLERSTRATEGY);
            //RMap rHitMap = redissonClient.getMap(RATELIMITER_HIT_CRAWLERSTRATEGY);
            if ((rRequestCount.increment() > requestMaxSize) || rHitMap.hasKey(ipAddress)) {
                //触发爬虫策略 ，默认10天后可重新访问
                long lockExpire = properties.getIpRule().getLockExpire();
                rExpirationTime.expire(lockExpire, TimeUnit.SECONDS);
                //保存触发来源
                rHitMap.put(ipAddress, requestUrl);
                LOGGER.info("Intercepted request, uri: {}, ip：{}, request :{}, times in {} ms。Automatically unlock after {} seconds", requestUrl, ipAddress, requestMaxSize, expirationTime,lockExpire);
                return true;
            }
        }
        return false;
    }

    /**
     * 重置已记录规则
     * @param request 请求
     * @param realRequestUri 原始请求uri
     */
    @Override
    public void reset(HttpServletRequest request, String realRequestUri) {
        String ipAddress = getIpAddr(request);
        String requestUrl = realRequestUri;
        /**
         * 重置计数器
         */
        int expirationTime = properties.getIpRule().getExpirationTime();
        //RAtomicLong rRequestCount = redissonClient.getAtomicLong(RATELIMITER_COUNT_PREFIX.concat(requestUrl).concat(ipAddress));
        BoundValueOperations<String,Long> rRequestCount = redisTemplate.boundValueOps(RATELIMITER_COUNT_PREFIX.concat(requestUrl).concat(ipAddress));

        // RAtomicLong rExpirationTime = redissonClient.getAtomicLong(RATELIMITER_EXPIRATIONTIME_PREFIX.concat(requestUrl).concat(ipAddress));
        BoundValueOperations<String,Long> rExpirationTime = redisTemplate.boundValueOps(RATELIMITER_EXPIRATIONTIME_PREFIX.concat(requestUrl).concat(ipAddress));

        rRequestCount.set(0L);
        rExpirationTime.set(0L,expirationTime, TimeUnit.MILLISECONDS);
        /**
         * 清除记录
         */
       // RMap rHitMap = redissonClient.getMap(RATELIMITER_HIT_CRAWLERSTRATEGY);
        BoundHashOperations<String,String,String> rHitMap = redisTemplate.boundHashOps(RATELIMITER_HIT_CRAWLERSTRATEGY);
        rHitMap.delete(ipAddress);
    }

    private static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return 0;
    }
    private final static Logger LOGGER = LoggerFactory.getLogger(IpRule.class);

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private AntiSpiderProperties properties;

    private static final String RATELIMITER_COUNT_PREFIX = "ratelimiter_request_count";
    private static final String RATELIMITER_EXPIRATIONTIME_PREFIX = "ratelimiter_expirationtime";
    private static final String RATELIMITER_HIT_CRAWLERSTRATEGY = "ratelimiter_hit_crawlerstrategy";
}
