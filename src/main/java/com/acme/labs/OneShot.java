
package com.acme.labs;

import org.quartz.Job;
import org.quartz.JobExecutionException;
import org.quartz.JobExecutionContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneShot implements Job {
    private final static Logger _log = LoggerFactory.getLogger(OneShot.class);
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        _log.debug("one shot!");
    }
}
