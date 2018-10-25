package com.sohu.tv.mq.common;

import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.mq.stats.StatsHelper;
/**
 * 对发送消息进行hook
 * 
 * @author yongfeigao
 * @date 2018年9月5日
 */
public class SohuSendMessageHook implements SendMessageHook {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    // 统计助手
    private StatsHelper statsHelper;
    
    public SohuSendMessageHook(DefaultMQProducer producer) {
        statsHelper = new StatsHelper();
        // 最大耗时，延后300毫秒
        statsHelper.init(producer.getSendMsgTimeout() + 300);
        // 客户端ip
        statsHelper.setClientHost(producer.getClientIP());
        // 获取生产者group
        statsHelper.setProducer(producer.getProducerGroup());
    }

    @Override
    public String hookName() {
        return "sohu";
    }

    @Override
    public void sendMessageBefore(SendMessageContext context) {
        context.setMqTraceContext(System.currentTimeMillis());
    }

    @Override
    public void sendMessageAfter(SendMessageContext context) {
        Object obj = context.getMqTraceContext();
        if(obj == null) {
            return;
        }
        long cost = System.currentTimeMillis() - (long) obj;
        try {
            statsHelper.increment(context.getBrokerAddr(), (int)cost, context.getException());
        } catch (Throwable e) {
            logger.warn("stats err", e);
        }
    }

    public StatsHelper getStatsHelper() {
        return statsHelper;
    }
}