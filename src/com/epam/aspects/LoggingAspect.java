package com.epam.aspects;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by Rauf_Aliev on 8/25/2016.
 */
@Aspect
public class LoggingAspect {

    private final static Logger LOGGER = Logger.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.epam.controllers.LoggingToolController(..))")
    public void theExample()
    {
        //empty
    }

    @Around("theExample()")
    public Object theMethod(final ProceedingJoinPoint pjp) throws Throwable {
        LOGGER.info("ASPECT INVOKED");
        Object o = pjp.proceed();
        return o;
    }


}
