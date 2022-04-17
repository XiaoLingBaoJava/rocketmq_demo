package com.xiaolingbao.withmq.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

@Service
public class DownStreamSystemService {

    @Qualifier("threadPool")
    @Autowired
    private ExecutorService executorService;

    private volatile ConcurrentHashMap<Long, Long> concurrentHashMap = new ConcurrentHashMap<>();

    public FutureTask<Long> cal(Long id) {
        FutureTask<Long> task = null;
        try {
           task = new FutureTask(new CalCallable(id));
           executorService.execute(task);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(Thread.currentThread().getName() + "处理id为" + id + "的请求失败,失败原因为" + e.getMessage());
        }
        return task;
    }

    public Long calAlogrithm(Long id) {
        long[] array = new long[100000];
        Arrays.fill(array, 1);
        Long result = id;
        for (int i = 0; i < 100000; i++) {
            for (int j = 0; j < 100000; j++) {
                array[i] = array[i] + array[j];
            }
        }
        for (Long aLong : array) {
            result += aLong;
        }
        return result;
    }

    class CalCallable implements Callable<Long> {

        private Long id;

        public CalCallable(Long id) {
            this.id = id;
        }

        @Override
        public Long call() {
            Long result = 0L;
            try {
                result = calAlogrithm(id);
                concurrentHashMap.put(id, result);
                System.out.println(Thread.currentThread().getName() + "处理id为" + id + "的请求成功,结果为" + result);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(Thread.currentThread().getName() + "处理id为" + id + "的请求失败,失败原因为" + e.getMessage());
            }
            return result;
        }
    }

}
