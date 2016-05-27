<?xml version="1.0" encoding="UTF-8"?>
<cron-task class="org.jpos.ext.groovy.CronTaskAdaptor" logger="Q2">
	<scheduler>
		<property name="org.quartz.threadPool.threadCount" value="1"/>
		<property name="org.quartz.scheduler.makeSchedulerThreadDaemon" value="true"/>
		<property name="org.quartz.scheduler.skipUpdateCheck" value="true"/>
	</scheduler>
	<task>
		<property name="script" value="cron/ForceLogout.groovy"/>
		<property name="cron" value="0 1 0 * * ?"/>
		<property name="triggerNow" value="false"/>
	</task>
</cron-task>
