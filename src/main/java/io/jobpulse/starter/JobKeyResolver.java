package io.jobpulse.starter;

import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

final class JobKeyResolver {

    private JobKeyResolver() {
    }

    static String resolve(Runnable runnable) {
        if (runnable instanceof ScheduledMethodRunnable scheduled) {
            try {
                Method method = scheduled.getMethod();
                Object target = scheduled.getTarget();
                return ClassUtils.getUserClass(target).getName() + "#" + method.getName();
            } catch (IllegalStateException delegatedRunnable) {
                return runnable.getClass().getName();
            }
        }
        return runnable.getClass().getName();
    }
}
