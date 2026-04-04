package com.mall.config.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    String key(); // 락의 이름 (SpEL 지원 예정)
    long waitTime() default 5L; // 락 획득 대기 시간 (초)
    long leaseTime() default 3L; // 락 점유 시간 (초)
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
