package io.jobpulse.starter;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

final class ScheduleDescriptor {

    private ScheduleDescriptor() {
    }

    static String of(Trigger trigger) {
        if (trigger instanceof CronTrigger cron) {
            return "cron:" + cron.getExpression();
        }
        return "trigger:" + trigger.getClass().getSimpleName();
    }
}
