
package com.acme.labs;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import org.quartz.JobExecutionException;
import org.quartz.JobExecutionContext;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.javatuples.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App implements Job {
    final static Logger logger = LoggerFactory.getLogger(App.class);

    final static CountDownLatch latch = new CountDownLatch(5);

    public static Pair<JobKey, TriggerKey> doSomething(Scheduler scheduler) throws SchedulerException {
        JobDetail job = newJob(App.class)
            .withIdentity("job1", "group1")
            .build();

        Trigger trigger = newTrigger()
            .withIdentity("trigger1", "group1")
            .startNow()
            .withSchedule(simpleSchedule()
                          .withIntervalInSeconds(1)
                          .repeatForever())
            .build();

        scheduler.scheduleJob(job, trigger);

        return Pair.with(job.getKey(), trigger.getKey());
    }

    public static void doSomethingImmediatelyAndOnce(Scheduler scheduler) throws SchedulerException {
        /* .withIdentity/2 is optional */

        JobDetail job = newJob(OneShot.class)
            .build();

        Trigger trigger = newTrigger()
            .startNow()
            .build();

        scheduler.scheduleJob(job, trigger);
    }

    public static void main(String[] args) {
        try {
            Scheduler scheduler = org.quartz.impl.StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            Pair<JobKey, TriggerKey> tuple = doSomething(scheduler);

            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new RuntimeException(String.format("%s timeout", latch));
            }

            scheduler.unscheduleJob(tuple.getValue1());
            scheduler.deleteJob(tuple.getValue0());
            logger.debug("job " + tuple.getValue0() + " and trigger " + tuple.getValue1() + " canceled");

            Thread.sleep(1000);

            doSomethingImmediatelyAndOnce(scheduler);

            Thread.sleep(250);

            logger.debug("sleeping for 3 seconds...");
            Thread.sleep(3000);

            logger.debug("scheduler shutdown");
            scheduler.shutdown();

        //} catch (SchedulerException se) {
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        latch.countDown();
        logger.debug("job running");
    }
}
