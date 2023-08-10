package com.chengzhx.distributionLocker;

import com.chengzhx.distributionLocker.util.RedissonLockUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.RedissonMultiLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author ChengZhenxing
 * @since 2023/5/12 09:37
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedissonLockTest {

    @Test
    public void testReentrantLock() throws InterruptedException {
        RLock lock = RedissonLockUtil.getLock("lockKey");
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                // 1. 最常见的使用方法
                // lock.lock();
                // 2. 支持过期解锁功能,10秒钟以后自动解锁, 无需调用unlock方法手动解锁
                // lock.lock(10, TimeUnit.SECONDS);
                // 3. 尝试加锁，最多等待3秒，上锁以后10秒自动解锁
                try {
                    boolean res = lock.tryLock(3, 10, TimeUnit.SECONDS);
                    if (res) {
                        System.out.println(Thread.currentThread().getName() + "线程获取锁成功");
                        //获得锁，执行业务
                    } else {
                        System.out.println(Thread.currentThread().getName() + "线程获取锁失败");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        //过期自动解锁,无需手动解锁
        //lock.unlock();
        Thread.sleep(10000);
    }


    @Test
    public void testAsyncReentrantLock() throws InterruptedException {
        new Thread(() -> {
            try {
                /*lock.tryLockAsync();
                //加锁10后自动解锁
                lock.tryLockAsync(10, TimeUnit.SECONDS);
                lock.tryLockAsync(3, 20, TimeUnit.SECONDS)*/
                RFuture<Boolean> res = RedissonLockUtil.tryLockAsync("getLock", 3, 20, TimeUnit.SECONDS);
                if (res.get()) {
                    System.out.println("这个是获取到锁之后，异步调用的方法");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        System.out.println("这是主线程的方法");
        Thread.sleep(10000);
    }

    /**
     * 公平锁（Fair Lock）
     * Redisson分布式可重入公平锁也是实现了java.util.concurrent.locks.Lock接口的一种RLock对象。
     * 在提供了自动过期解锁功能的同时，保证了当多个Redisson客户端线程同时请求加锁时，优先分配给先发出请求的线程。
     */
    @Test
    public void testFairLock() throws InterruptedException {
        RLock fairLock = RedissonLockUtil.getFairLock("getLock");
        new Thread(new MyRunner("线程A", fairLock)).start();
        new Thread(new MyRunner("线程B", fairLock)).start();
        Thread.sleep(10000);
    }

    /**
     * 联锁（MultiLock）
     * Redisson的RedissonMultiLock对象可以将多个RLock对象关联为一个联锁，
     * 每个RLock对象实例可以来自于不同的Redisson实例
     * 所有的锁都上锁成功才算成功，只要有一个锁失败，则失败。
     */
    @Test
    public void testMultiLock() {
        RLock lock1 = RedissonLockUtil.getLock("lock1");
        RLock lock2 = RedissonLockUtil.getLock("lock2");
        RLock lock3 = RedissonLockUtil.getLock("lock3");
        RedissonMultiLock multiLock = new RedissonMultiLock(lock1, lock2, lock3);
        try {
            //multiLock.lock();
            //multiLock.lock(100,TimeUnit.SECONDS);
            boolean res = multiLock.tryLock(10, 10, TimeUnit.SECONDS);
            if (res) {
                System.out.println("这是联锁测试");
            }
        } catch (Exception e) {
            System.out.println("获取不到锁，发生异常");
            e.printStackTrace();
        } finally {
            try {
                multiLock.unlock();
            } catch (Exception e) {
                System.out.println("获取不到锁，解锁发生异常");
            }
        }
    }

    @Test
    public void testAlwaysLock() throws InterruptedException {
        RLock lock = RedissonLockUtil.getLock("lock1");
        new Thread(() -> {
            // 1. 最常见的使用方法
            // lock.lock();
            // 2. 支持过期解锁功能,10秒钟以后自动解锁, 无需调用unlock方法手动解锁
            // lock.lock(10, TimeUnit.SECONDS);
            // 3. 尝试加锁，最多等待3秒，上锁以后10秒自动解锁
            try {
                boolean res = lock.tryLock();
                if (res) {
                    System.out.println(Thread.currentThread().getName() + "线程获取锁成功");
                    //获得锁，执行业务
                } else {
                    System.out.println(Thread.currentThread().getName() + "线程获取锁失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(10000);
    }

    /**
     * 红锁（RedLock）
     * Redisson的RedissonRedLock对象实现了Redlock介绍的加锁算法。
     * 该对象也可以用来将多个RLock对象关联为一个红锁，每个RLock对象实例可以来自于不同的Redisson实例
     * 红锁在大部分节点上加锁成功就算成功。
     */
    @Test
    public void testRedLock() {
        RLock lock1 = RedissonLockUtil.getLock("lock1");
        RLock lock2 = RedissonLockUtil.getLock("lock2");
        RLock lock3 = RedissonLockUtil.getLock("lock3");
        RedissonRedLock lock = new RedissonRedLock(lock1, lock2, lock3);
        try {
            // 同时加锁：lock1 lock2 lock3, 红锁在大部分节点上加锁成功就算成功。
            lock.lock();
            // 尝试加锁，最多等待10秒，上锁以后10秒自动解锁
            boolean res = lock.tryLock(10, 10, TimeUnit.SECONDS);
            if (res) {
                System.out.println("这是红锁测试");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 读写锁（ReadWriteLock）
     * Redisson的分布式可重入读写锁RReadWriteLock Java对象实现了java.util.concurrent.locks.ReadWriteLock接口。
     * 该对象允许同时有多个读取锁，但是最多只能有一个写入锁。
     *
     * @throws InterruptedException 异常
     */
    @Test
    public void testRWlock() throws InterruptedException {
        RReadWriteLock rwlock = RedissonLockUtil.getReadWriteLock("lockKey");
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                try {
                    boolean redRes = rwlock.readLock().tryLock(5, 8, TimeUnit.SECONDS);
                    if (redRes) {
                        System.out.println(Thread.currentThread().getName() + "线程获取读锁成功");
                    } else {
                        System.out.println(Thread.currentThread().getName() + "线程获取读锁失败");
                    }
                    boolean writeRes = rwlock.writeLock().tryLock(10, 10, TimeUnit.SECONDS);
                    if (writeRes) {
                        System.out.println(Thread.currentThread().getName() + "线程获取写锁成功");
                    } else {
                        System.out.println(Thread.currentThread().getName() + "线程获取写锁失败");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        Thread.sleep(20000);
    }

    @Test
    public void testSemaphore() throws InterruptedException {
        RSemaphore semaphore = RedissonLockUtil.getSemaphore("semaphore ");
        semaphore.trySetPermits(10);
        Driver driver = new Driver(semaphore);
        for (int i = 0; i < 5; i++) {
            (new Car(driver)).start();
        }
        Thread.sleep(10000);
    }

    @Test
    public void testCountDownLatch() throws InterruptedException {
        RCountDownLatch countDownLatch = RedissonLockUtil.getCountDownLatch("countDownLatch ");
        countDownLatch.trySetCount(5);
        Driver driver = new Driver(countDownLatch);
        for (int i = 0; i < 5; i++) {
            (new Car(driver)).start();
        }
        System.out.println("主线程阻塞,等待所有子线程执行完成");
        //countDownLatch.await()使得主线程阻塞直到countDownLatch.countDown()为零才继续执行
        countDownLatch.await();
        System.out.println("所有线程执行完成!");
        Thread.sleep(10000);
    }


    class MyRunner implements Runnable {
        String threadName;
        RLock rLock;

        public MyRunner(String threadName, RLock rLock) {
            this.threadName = threadName;
            this.rLock = rLock;
        }

        public String getThreadName() {
            return threadName;
        }

        public void setThreadName(String threadName) {
            this.threadName = threadName;
        }

        public RLock getrLock() {
            return rLock;
        }

        public void setrLock(RLock rLock) {
            this.rLock = rLock;
        }

        @Override
        public void run() {
            try {
                /*
                fairLock.lock();
                // 支持过期解锁功能, 10秒钟以后自动解锁,无需调用unlock方法手动解锁
                fairLock.lock(10, TimeUnit.SECONDS);*/
                // 尝试加锁，最多等待100秒，上锁以后10秒自动解锁
                boolean res = rLock.tryLock(10, 10, TimeUnit.SECONDS);
                if (res) {
                    System.out.println("线程名称:" + getThreadName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class Driver {
        private RSemaphore semaphore;
        private RCountDownLatch rCountDownLatch;

        public Driver(RSemaphore semaphore) {
            this.semaphore = semaphore;
        }

        public Driver(RCountDownLatch rCountDownLatch) {
            this.rCountDownLatch = rCountDownLatch;
        }

        public void driveCar() {
            try {
                // 从信号量中获取一个允许机会
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " start at " + System.currentTimeMillis());
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName() + " stop at " + System.currentTimeMillis());
                // 释放允许，将占有的信号量归还
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void driveCar2() {
            System.out.println(Thread.currentThread().getName() + " start at " + System.currentTimeMillis());
            // 每个独立子线程执行完后,countDownLatch值减1
            rCountDownLatch.countDown();
        }
    }

    class Car extends Thread {
        private Driver driver;

        public Car(Driver driver) {
            super();
            this.driver = driver;
        }

        public void run() {
            //driver.driveCar();
            driver.driveCar2();
        }
    }

}
