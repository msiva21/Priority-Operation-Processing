package com.comcast.pop.modules.monitor.alive;

import com.comcast.pop.modules.monitor.PropertyLoader;
import com.comcast.pop.modules.monitor.config.ConfigurationProperties;
import com.comcast.pop.modules.monitor.metric.LoggingMetricReporterFactory;
import com.comcast.pop.modules.monitor.metric.MetricAliveCheckListener;
import com.comcast.pop.modules.monitor.metric.MetricReporter;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

public class AliveCheckTest implements AliveCheck
{
    private Random random = new Random();

    @Override
    public boolean isAlive()
    {
        return random.nextBoolean();
    }
    @Test(enabled = false)
    public void testAlive() throws Exception
    {
        Properties serviceProperties = PropertyLoader.loadResource("../../../../../../service.properties");
        MetricReporter reporter = new MetricReporter(null);
        reporter.register(new LoggingMetricReporterFactory());
        LogAliveCheckListener loggerAliveCheck = new LogAliveCheckListener();
        MetricAliveCheckListener metricsAliveCheck = new MetricAliveCheckListener(reporter);
        ConfigurationProperties configurationProperties = ConfigurationProperties.from(serviceProperties, new AliveCheckConfigKeys());
        AliveCheckPoller poller = new AliveCheckPoller(configurationProperties, this, Arrays.asList(loggerAliveCheck, metricsAliveCheck));
        poller.start();
        long endTime = (configurationProperties.get(AliveCheckConfigKeys.CHECK_FREQUENCY) * 10);
        Thread.sleep(endTime);
    }
}
