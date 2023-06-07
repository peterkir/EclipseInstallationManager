package impl;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.oomph.setup.Installation;
import org.eclipse.oomph.setup.LocationCatalog;
import org.eclipse.oomph.setup.Workspace;
import org.eclipse.oomph.setup.internal.core.util.SetupCoreUtil;
import org.osgi.service.component.annotations.Component;

import eim.api.ListLocationService;

@Component
public class LocationListServiceImpl implements ListLocationService {
	
	@Override
	public void listLocations() {
		String oomphhome = System.getProperty("user.home", System.getenv("HOME"))
				.concat("/.eclipse/org.eclipse.oomph.setup");
		String setupfile = oomphhome + "/setups/locations.setup";
		File file = new File(setupfile);
		URI uri = URI.createFileURI(file.getAbsolutePath());
		System.out.println("Loading EMF model " + uri);

		ResourceSet resourceSet = SetupCoreUtil.createResourceSet();

		try {
			// load resource for this file //
			Resource resource = resourceSet.getResource(uri, true);
			System.out.println("Loaded " + uri);

			// iterate contents of the loaded resource.
			for (EObject eObject : resource.getContents()) {
				if (eObject instanceof LocationCatalog) {
					LocationCatalog catalog = (LocationCatalog) eObject;
					System.out.println("# Installations");
					Iterator<Entry<Installation, EList<Workspace>>> instIterator = catalog.getInstallations()
							.iterator();
					while (instIterator.hasNext()) {
						Entry<Installation, EList<Workspace>> instEntry = instIterator.next();
						Installation inst = instEntry.getKey();
						System.out.format("## installation inside <%s>\n", inst.eResource().getURI());
						instEntry.getValue().forEach(w -> System.out.println("   wrkspc: " + w.eResource().getURI()));
					}
				} else {
					System.out.println("no LocationCatalog found");
				}
			}
		} catch (RuntimeException exception) {
			System.out.println("Problem loading " + uri);
			exception.printStackTrace();
		}

		System.out.println("done");

	}
}
