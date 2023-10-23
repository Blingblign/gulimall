package com.zzclearning.springcloud.rule;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author bling
 * @create 2023-10-13 11:15
 */

public class MyRule extends AbstractLoadBalancerRule {
    private static final Logger log = LoggerFactory.getLogger(MyRule.class);
    private AtomicInteger current = new AtomicInteger(0);
    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object key) {
        int next;
        List<Server> allServers = getLoadBalancer().getAllServers();
        if (allServers.size() == 0)
            return null;
        do {
            next = current.get() >= Integer.MAX_VALUE ? 0 : current.get()+1;
            log.info("第" + next + "次请求");
        } while (!current.compareAndSet(current.get(),next));
        return allServers.get(next%allServers.size());
    }
}
