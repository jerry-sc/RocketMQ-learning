/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.client.impl.consumer;

import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.common.ServiceThread;
import org.slf4j.Logger;

/**
 * 均衡消息队列服务，负责分配当前 Consumer 可消费的消息队列( MessageQueue )。当有新的 Consumer 的加入或移除，都会重新分配消息队列
 *
 * 目前有三种情况会触发均衡消息队列服务
 * 1. 等待超时，每 20s 调用一次。
 * 2. PushConsumer 启动时，调用 rebalanceService#wakeup(...) 触发。
 * 3. Broker 通知 Consumer 加入 或 移除时，Consumer 响应通知，调用 rebalanceService#wakeup(...) 触发。
 */
public class RebalanceService extends ServiceThread {
    /**
     * 定期会进行负载均衡调用
     */
    private static long waitInterval =
        Long.parseLong(System.getProperty(
            "rocketmq.client.rebalance.waitInterval", "20000"));
    private final Logger log = ClientLogger.getLog();
    private final MQClientInstance mqClientFactory;

    public RebalanceService(MQClientInstance mqClientFactory) {
        this.mqClientFactory = mqClientFactory;
    }

    @Override
    public void run() {
        log.info(this.getServiceName() + " service started");

        while (!this.isStopped()) {
            this.waitForRunning(waitInterval);
            this.mqClientFactory.doRebalance();
        }

        log.info(this.getServiceName() + " service end");
    }

    @Override
    public String getServiceName() {
        return RebalanceService.class.getSimpleName();
    }
}
