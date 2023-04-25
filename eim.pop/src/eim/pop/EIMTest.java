package eim.pop;

import static org.knowhowlab.osgi.testing.assertions.BundleAssert.assertBundleState;
import static org.knowhowlab.osgi.testing.assertions.OSGiAssert.getBundleContext;
import static org.knowhowlab.osgi.testing.assertions.ServiceAssert.assertServiceAvailable;
import static org.knowhowlab.osgi.testing.assertions.ServiceAssert.assertServiceUnavailable;
import static org.knowhowlab.osgi.testing.utils.BundleUtils.findBundle;

import java.util.concurrent.TimeUnit;

import org.knowhowlab.osgi.testing.utils.ServiceUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import eim.api.EclipseService;
import junit.framework.TestCase;

public class EIMTest extends TestCase {
	
	private final String SERVICE_BUNDLE = "eim";
	private final BundleContext context = FrameworkUtil.getBundle(EIMTest.class).getBundleContext();
	
	public void testLifeCycle() throws Exception {
		assertBundleState(Bundle.ACTIVE, SERVICE_BUNDLE);
		assertServiceAvailable(EclipseService.class);
		findBundle(getBundleContext(), SERVICE_BUNDLE).stop();
		assertServiceUnavailable(EclipseService.class, 1, TimeUnit.SECONDS);
		findBundle(getBundleContext(), SERVICE_BUNDLE).start();
		EclipseService eclService = ServiceUtils.getService(context, EclipseService.class);
		assertNotNull(eclService);
	}

}