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
package net.javacrumbs.shedlock.test.support;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractLockProviderIntegrationTest {
    protected static final String LOCK_NAME1 = "name";
    public static final Duration LOCK_AT_LEAST_FOR = Duration.of(2, SECONDS);

    protected abstract LockProvider getLockProvider();

    protected abstract void assertUnlocked(String lockName);

    protected abstract void assertLocked(String lockName);


    @Test
    public void shouldCreateLock() {
        Optional<SimpleLock> lock = getLockProvider().lock(lockConfig(LOCK_NAME1));
        assertThat(lock).isNotEmpty();

        assertLocked(LOCK_NAME1);
        lock.get().unlock();
        assertUnlocked(LOCK_NAME1);

    }

    @Test
    public void shouldNotReturnSecondLock() {
        Optional<SimpleLock> lock = getLockProvider().lock(lockConfig(LOCK_NAME1));
        assertThat(lock).isNotEmpty();
        assertThat(getLockProvider().lock(lockConfig(LOCK_NAME1))).isEmpty();
        lock.get().unlock();
    }

    @Test
    public void shouldCreateTwoIndependentLocks() {
        Optional<SimpleLock> lock1 = getLockProvider().lock(lockConfig(LOCK_NAME1));
        assertThat(lock1).isNotEmpty();

        Optional<SimpleLock> lock2 = getLockProvider().lock(lockConfig("name2"));
        assertThat(lock2).isNotEmpty();

        lock1.get().unlock();
        lock2.get().unlock();
    }

    @Test
    public void shouldLockTwiceInARow() {
        Optional<SimpleLock> lock1 = getLockProvider().lock(lockConfig(LOCK_NAME1));
        assertThat(lock1).isNotEmpty();
        lock1.get().unlock();

        Optional<SimpleLock> lock2 = getLockProvider().lock(lockConfig(LOCK_NAME1));
        assertThat(lock2).isNotEmpty();
        lock2.get().unlock();
    }

    @Test
    public void shouldTimeout() throws InterruptedException {
        LockConfiguration configWithShortTimeout = lockConfig(LOCK_NAME1, Duration.ofMillis(20), Duration.ZERO);
        Optional<SimpleLock> lock1 = getLockProvider().lock(configWithShortTimeout);
        assertThat(lock1).isNotEmpty();

        sleep(25);
        assertUnlocked(LOCK_NAME1);

        Optional<SimpleLock> lock2 = getLockProvider().lock(lockConfig(LOCK_NAME1, Duration.ofMillis(5), Duration.ZERO));
        assertThat(lock2).isNotEmpty();
        lock2.get().unlock();
    }


    @Test
    public void shouldBeAbleToLockRightAfterUnlock() {
        LockConfiguration lockConfiguration = lockConfig(LOCK_NAME1);
        LockProvider lockProvider = getLockProvider();
        for (int i = 0; i < 100; i++) {
            Optional<SimpleLock> lock = lockProvider.lock(lockConfiguration);
            assertThat(lockProvider.lock(lockConfiguration)).isEmpty();
            assertThat(lock).isNotEmpty();
            lock.get().unlock();
        }
    }

    @Test
    public void fuzzTestShouldPass() throws ExecutionException, InterruptedException {
        new FuzzTester(getLockProvider()).doFuzzTest();
    }

    @Test
    public void shouldLockAtLeastFor() throws InterruptedException {
        Optional<SimpleLock> lock1 = getLockProvider().lock(lockConfig(LOCK_NAME1, LOCK_AT_LEAST_FOR.multipliedBy(2), LOCK_AT_LEAST_FOR));
        assertThat(lock1).isNotEmpty();
        lock1.get().unlock();

        // can not acquire lock, grace period did not pass yet
        assertThat(getLockProvider().lock(lockConfig(LOCK_NAME1))).isEmpty();
        sleep(LOCK_AT_LEAST_FOR.toMillis());

        // can acquire the lock
        Optional<SimpleLock> lock3 = getLockProvider().lock(lockConfig(LOCK_NAME1));
        assertThat(lock3).isNotEmpty();
        lock3.get().unlock();

    }

    protected static LockConfiguration lockConfig(String name) {
        return lockConfig(name, Duration.of(5, MINUTES), Duration.ZERO);
    }

    protected static LockConfiguration lockConfig(String name, Duration lockAtMostFor, Duration lockAtLeastFor) {
        Instant now = Instant.now();
        return new LockConfiguration(name, now.plus(lockAtMostFor), now.plus(lockAtLeastFor));
    }

}