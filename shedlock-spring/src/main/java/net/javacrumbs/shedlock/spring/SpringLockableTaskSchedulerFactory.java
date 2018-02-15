/**
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.shedlock.spring;

import net.javacrumbs.shedlock.core.DefaultLockManager;
import net.javacrumbs.shedlock.core.LockProvider;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.temporal.TemporalAmount;
import java.util.concurrent.ScheduledExecutorService;

import static net.javacrumbs.shedlock.spring.SpringLockConfigurationExtractor.DEFAULT_LOCK_AT_MOST_FOR;

/**
 * Helper class to simplify configuration of Spring LockableTaskScheduler.
 * @deprecated Use {@link ScheduledLockConfigurationBuilder} instead. Objects created through this class will not support string annotation param resolution.
 */
@Deprecated
public class SpringLockableTaskSchedulerFactory {

    /**
     * Wraps the task scheduler and ensures that {@link net.javacrumbs.shedlock.core.SchedulerLock} annotated methods
     * are locked using the lockProvider
     *
     * @param taskScheduler        wrapped task scheduler
     * @param lockProvider         lock provider to be used
     * @param defaultLockAtMostFor default value of lockAtMostFor if none specified in the annotation
     */
    public static LockableTaskScheduler newLockableTaskScheduler(TaskScheduler taskScheduler, LockProvider lockProvider, TemporalAmount defaultLockAtMostFor) {
        return new LockableTaskScheduler(taskScheduler, new DefaultLockManager(lockProvider, new SpringLockConfigurationExtractor(defaultLockAtMostFor)));
    }

    /**
     * Wraps the task scheduler and ensures that {@link net.javacrumbs.shedlock.core.SchedulerLock} annotated methods
     * are locked using the lockProvider
     *
     * @param taskScheduler wrapped task scheduler
     * @param lockProvider  lock provider to be used
     */
    public static LockableTaskScheduler newLockableTaskScheduler(TaskScheduler taskScheduler, LockProvider lockProvider) {
        return newLockableTaskScheduler(taskScheduler, lockProvider, DEFAULT_LOCK_AT_MOST_FOR);
    }

    /**
     * Wraps ScheduledExecutorService and ensures that {@link net.javacrumbs.shedlock.core.SchedulerLock} annotated methods
     * are locked using the lockProvider
     *
     * @param scheduledExecutorService wrapped ScheduledExecutorService
     * @param lockProvider             lock provider to be used
     */
    public static LockableTaskScheduler newLockableTaskScheduler(ScheduledExecutorService scheduledExecutorService, LockProvider lockProvider) {
        return newLockableTaskScheduler(new ConcurrentTaskScheduler(scheduledExecutorService), lockProvider);
    }

    /**
     * Wraps ScheduledExecutorService and ensures that {@link net.javacrumbs.shedlock.core.SchedulerLock} annotated methods
     * are locked using the lockProvider
     *
     * @param scheduledExecutorService wrapped ScheduledExecutorService
     * @param lockProvider             lock provider to be used
     * @param defaultLockAtMostFor     default value of lockAtMostFor if none specified in the annotation
     */
    public static LockableTaskScheduler newLockableTaskScheduler(ScheduledExecutorService scheduledExecutorService, LockProvider lockProvider, TemporalAmount defaultLockAtMostFor) {
        return newLockableTaskScheduler(new ConcurrentTaskScheduler(scheduledExecutorService), lockProvider, defaultLockAtMostFor);
    }

    /**
     * Creates {@link ThreadPoolTaskScheduler} and ensures that {@link net.javacrumbs.shedlock.core.SchedulerLock} annotated methods
     * are locked using the lockProvider
     *
     * @param poolSize     size of the thread pool
     * @param lockProvider lock provider to be used
     */
    public static LockableTaskScheduler newLockableTaskScheduler(int poolSize, LockProvider lockProvider) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(poolSize);
        taskScheduler.initialize();
        return newLockableTaskScheduler(taskScheduler, lockProvider);
    }
}
