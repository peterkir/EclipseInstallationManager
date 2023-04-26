package eim.pop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.knowhowlab.osgi.testing.assertions.BundleAssert.assertBundleState;
import static org.knowhowlab.osgi.testing.assertions.OSGiAssert.getBundleContext;
import static org.knowhowlab.osgi.testing.assertions.ServiceAssert.assertServiceAvailable;
import static org.knowhowlab.osgi.testing.assertions.ServiceAssert.assertServiceUnavailable;
import static org.knowhowlab.osgi.testing.utils.BundleUtils.findBundle;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.knowhowlab.osgi.testing.utils.ServiceUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import eim.api.EclipseService;

public class EIMTest {
	
	private final String SERVICE_BUNDLE = "eim.impl";
	private final BundleContext context = FrameworkUtil.getBundle(EIMTest.class).getBundleContext();
	private static String command;
	
	@BeforeAll
	public static void setUp() {
		String os = System.getProperty("os.name").toLowerCase();
		if(os.contains("windows")) {
			command="C:/Windows/System32/notepad.exe";
		} else {
			command="/bin/bash";
		}
	}
	
	
	@Test
	public void testLifeCycle() throws Exception {
		assertBundleState(Bundle.ACTIVE, SERVICE_BUNDLE);
		assertServiceAvailable(EclipseService.class);
		findBundle(getBundleContext(), SERVICE_BUNDLE).stop();
		assertServiceUnavailable(EclipseService.class, 1, TimeUnit.SECONDS);
		findBundle(getBundleContext(), SERVICE_BUNDLE).start();
		EclipseService eclService = ServiceUtils.getService(context, EclipseService.class);
		assertNotNull(eclService);
	}
	
	@Test
	public void testStartProcess() {
		EclipseService eclService = ServiceUtils.getService(context, EclipseService.class);
		Process p = eclService.startProcess(command, System.getProperty("user.home"), null);
		long parentPID = getPidViaRuntimeMXBean();
		long pid = p.pid();
		
		Optional<ProcessHandle> processHandle = ProcessHandle.of(parentPID);
		processHandle.ifPresent(process -> {
			process.children().forEach(child -> {
				assertEquals(pid, child.pid());
			});
		});
		
		
		
	}
	
	@AfterAll
	public static void cleanUp() {
		long parentPID = getPidViaRuntimeMXBean();
		Optional<ProcessHandle> processHandle = ProcessHandle.of(parentPID);
		processHandle.ifPresent(process -> {
			process.children().forEach(child -> {
				child.destroy();
			});
		});
	}
	
	private static Long getPidViaRuntimeMXBean() {
		RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
		String processName = rtb.getName();
		Long result = null;
		Pattern pattern = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(processName);
		if (matcher.matches()) {
			result = Long.valueOf(matcher.group(1));
		}
		return result;
	}

}