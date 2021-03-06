package com.comcast.pop.modules.kube.fabric8.test;

import com.comcast.pop.modules.kube.client.CpuRequestModulator;
import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import com.comcast.pop.modules.kube.fabric8.client.annotation.PodAnnotationClient;
import com.comcast.pop.modules.kube.fabric8.client.facade.KubernetesClientFacade;
import com.comcast.pop.modules.kube.fabric8.client.follower.PodFollower;
import com.comcast.pop.modules.kube.fabric8.client.follower.PodFollowerImpl;
import com.comcast.pop.modules.kube.fabric8.client.modulator.HiLowCpuRequestModulator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class AnnotationEditingTest extends KubeClientTestBase
{

    @Test
    public void testAnnotationsCanBeAdded()
    {
        PodConfig podConfig = TestPodConfigType.quickPod.createPodConfig(configFactory);
        podConfig.setReapCompletedPods(false);

        CpuRequestModulator cpuModulator = new HiLowCpuRequestModulator();
        ExecutionConfig executionConfig = new ExecutionConfig(podConfig.getNamePrefix());
        executionConfig.setCpuRequestModulator(cpuModulator);

        PodFollower podFollower = new PodFollowerImpl(configFactory.getDefaultKubeConfig(), podConfig, executionConfig);
        podFollower.startAndFollowPod(podFollower.getDefaultLogLineObserver(executionConfig));

        KubernetesClientFacade fabric8Client = podFollower.getPodPushClient().getKubernetesHttpClients().getRequestClient();
        PodAnnotationClient podAnnotationClient = new PodAnnotationClient(fabric8Client, executionConfig.getName());

        // Round 1, ensure that old annotations don't get blown away
        Map<String, String> annotations = new HashMap<>();
        annotations.put("ENCODE_RESULT", "JSON");

        podAnnotationClient.editPodAnnotations(annotations);

        // round 2
        Map<String, String> annotations2nd = new HashMap<>();
        annotations2nd.put("ENCODE_RESULT_2", "JSON");

        podAnnotationClient.editPodAnnotations(annotations2nd);

        Map<String, String> actualAnnotations = fabric8Client.getPodAnnotations(executionConfig.getName());

        try
        {
            annotations.forEach((i, j) ->
                {
                    Assert.assertTrue(actualAnnotations.containsKey(i));
                    Assert.assertTrue(actualAnnotations.get(i).equals(j));
                }
            );

            annotations2nd.forEach((i, j) ->
                {
                    Assert.assertTrue(actualAnnotations.containsKey(i));
                    Assert.assertTrue(actualAnnotations.get(i).equals(j));
                }
            );
        }
        finally
        {
            podFollower.getPodPushClient().deletePod(executionConfig.getName());
        }
    }
}
