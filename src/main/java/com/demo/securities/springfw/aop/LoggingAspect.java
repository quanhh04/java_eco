package com.demo.securities.springfw.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Bao quanh moi method cua TaiKhoanFwService bang 1 proxy (do @EnableAspectJAutoProxy
 * tao ra) - log ten method + thoi gian chay. Minh hoa AOP: code nghiep vu trong service
 * khong biet gi ve chuyen logging nay, no duoc "dan" vao tu ben ngoai qua pointcut.
 */
@Aspect
public class LoggingAspect {

    @Around("execution(* com.demo.securities.springfw.service.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            System.out.println("[AOP] " + joinPoint.getSignature().toShortString() + " mat " + duration + "ms");
        }
    }
}
