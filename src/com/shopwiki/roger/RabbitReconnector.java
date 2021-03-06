/*
 * Copyright [2012] [ShopWiki]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shopwiki.roger;

import com.rabbitmq.client.*;
import com.shopwiki.roger.util.TimeUtil;

/**
 * Used to re-establish a Connection to a RabbitMQ when a connection breaks.
 * Add an instance of this a Connection to use it.
 *
 * @author rstewart
 */
public class RabbitReconnector implements ShutdownListener, Runnable {

    public static interface ReconnectHandler {
        boolean reconnect() throws Exception;
    }

    public static interface ReconnectLogger {
        void log(ShutdownSignalException cause);
        void log(int attempt);
    }

    private final ReconnectHandler handler;
    private final ReconnectLogger logger;
    private final int secondsBeforeRetry;

    public RabbitReconnector(ReconnectHandler handler, int secondsBeforeRetry) {
        this(handler, null, secondsBeforeRetry);
    }

    public RabbitReconnector(ReconnectHandler handler, ReconnectLogger logger, int secondsBeforeRetry) {
        this.handler = handler;
        this.logger = logger;
        this.secondsBeforeRetry = secondsBeforeRetry;
    }

    @Override
    public void shutdownCompleted(ShutdownSignalException cause) {
        System.err.println(TimeUtil.now() + " RabbitMQ connection SHUTDOWN!");
        System.err.print("CAUSE: ");
        cause.printStackTrace();
        run();
    }

    private volatile long timeLastRun = System.currentTimeMillis();

    @Override
    public synchronized void run() {
        System.err.println(TimeUtil.now() + " Attempting to reconnect to RabbitMQ...");
        long millisBeforeRetry = secondsBeforeRetry * 1000;
        int attempt = 0;

        while (true) {
            attempt++;

            long millisSinceLastTry = System.currentTimeMillis() - timeLastRun;
            long millisToWait = millisBeforeRetry - millisSinceLastTry;
            if (millisToWait > 0) {
                System.err.println(TimeUtil.now() + " RabbitMQ reconnect # " + attempt + " too soon!  Waiting for " + millisToWait + " millis...");
                sleep(millisToWait);
            }

            try {
                timeLastRun = System.currentTimeMillis();
                if (handler.reconnect()) {
                    System.err.println(TimeUtil.now() + " RabbitMQ reconnect # " + attempt + " SUCCEEDED!");
                    return;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (logger != null) {
                logger.log(attempt);
            }
            System.err.println(TimeUtil.now() + " RabbitMQ reconnect # " + attempt + " FAILED!  Retrying in " + secondsBeforeRetry + " seconds...");
            sleep(millisBeforeRetry);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //throw new RuntimeException(e);
        }
    }
}
