package impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eim.api.EIMService;
import eim.api.LocationCatalogEntry;

/**
 * This class controls all aspects of the application's execution
 */
@Component
public class EIMServiceImpl implements EIMService {

	private static final Logger logger = LoggerFactory.getLogger(EIMServiceImpl.class);
	private LinkedList<LocationCatalogEntry> locationEntries = new LinkedList<>();

	@Override
	public Process startProcess(String command, String workingDir, String[] args) {
		Map<String, String> arguments = new HashMap<String, String>();

		ProcessBuilder pb = new ProcessBuilder();
		if (workingDir != null) {
			pb.directory(Paths.get(workingDir).toFile());
		}
		Map<String, String> env = pb.environment();

		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			env.put("TEMP", System.getenv("TEMP"));
			env.put("SYSTEMDRIVE", System.getenv("SYSTEMDRIVE"));
		}

		if (args != null && args.length > 0) {
			for (String string : args) {
				String[] argument = string.split("=");
				env.put(argument[0], argument[1]);
				arguments.put(argument[0], argument[1]);
			}
		}
		if (arguments.containsKey("ws")) {
			pb.command(command, "-data", arguments.get("ws"));
		} else {
			pb.command(command);
		}
		try {
			return pb.start();
		} catch (IOException e) {
			logger.error(
					"There was a problem starting the the process!\n" + "Command: " + command + "\n" + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public void startEntry(LocationCatalogEntry entryToExecute) {
		if (entryToExecute == null) {
			logger.error("The entry could not be found in the list.");
		} else {
			Path installationPath = entryToExecute.getInstallationPath();
			logger.debug("Installation path is " + installationPath.toAbsolutePath().toString());
			Path workspacePath = entryToExecute.getWorkspacePath();
			logger.debug("Workspace path is " + workspacePath.toAbsolutePath().toString());

			System.getProperty("eclipse.launcher.name");
			Path programPath = installationPath.resolve("eclipse.exe");
			ArrayList<String> args = new ArrayList<String>();
			args.add("ws=" + workspacePath.toString());
			String[] simpleArray = new String[args.size()];
			args.toArray(simpleArray);

			logger.debug("Starting " + programPath.toString() + " in working directory " + installationPath.toString()
					+ " with arguments\n " + Arrays.toString(simpleArray));
			startProcess(programPath.toString(), installationPath.toString(), simpleArray);
		}
	}

	@Override
	public void listLocations(String locationFile) {
		File file;
		if (locationFile == null || locationFile.isBlank()) {
			logger.debug("Utilizing the default locations.setup file of the Eclipse Installer.");
			String oomphhome = System.getProperty("user.home", System.getenv("HOME"))
					.concat("/.eclipse/org.eclipse.oomph.setup");
			String setupfile = oomphhome + "/setups/locations.setup";
			file = new File(setupfile);
		} else {
			logger.debug("Loading locations catalog from " + locationFile);
			file = new File(locationFile);
		}

		// refresh map entries
		try {
			fetchEntries(file);
		} catch (Exception e) {
			logger.error("The given file " + locationFile
					+ " is not a valid locationCatalog.\n Please check if that is the correct file!");
			e.printStackTrace();
		}

		// print map details
		for (LocationCatalogEntry entry : locationEntries) {
			Integer key = entry.getID();
			Path instPath = entry.getInstallationPath();
			Path wrkspcPath = entry.getWorkspacePath();
			System.out.format("Launch entry Number %s\n" + "Location: <%s>\n" + "Workspace: <%s>\n", key, instPath,
					wrkspcPath);
		}

	}

	private Resource loadLocationResource(File file) throws RuntimeException {
		URI uri = URI.createFileURI(file.getAbsolutePath());
		logger.debug("Loading locations catalog from " + uri);

		ResourceSet resourceSet = SetupCoreUtil.createResourceSet();

		Resource resource = resourceSet.getResource(uri, true);
		logger.debug("Loaded locations catalog from " + uri);

		return resource;
	}

	public void fetchEntries(File file) throws Exception {
		LinkedList<LocationCatalogEntry> entryListTemp = new LinkedList<>();
		try {
			Resource resource = loadLocationResource(file);

			// iterate contents of the loaded resource.
			int i = 1;
			for (EObject eObject : resource.getContents()) {
				if (eObject instanceof LocationCatalog) {
					LocationCatalog catalog = (LocationCatalog) eObject;
					System.out.println("# Installations");
					Iterator<Entry<Installation, EList<Workspace>>> instIterator = catalog.getInstallations()
							.iterator();
					while (instIterator.hasNext()) {
						Entry<Installation, EList<Workspace>> instEntry = instIterator.next();
						Installation inst = instEntry.getKey();
						Resource eResource = inst.eResource();
						if (eResource != null) {
							EList<Workspace> wrkspcList = instEntry.getValue();
							for (Workspace wrkspc : wrkspcList) {
								LocationCatalogEntry entry = new LocationCatalogEntryImpl(i, inst, wrkspc, null);
								logger.debug("Added entry " + entry.getInstallationFolderName() + " with workspace " + entry.getWorkspaceFolderName());
								entryListTemp.add(entry);
								i++;
							}
						} else {
							System.out.format("## installation <%s>\n", inst.getName());
						}
					}
					locationEntries = entryListTemp;
				} else {
					throw new Exception("The given file is not a LocationCatalog!");
				}
			}

		} catch (RuntimeException exception) {
			exception.printStackTrace();
		}

	}

	public LinkedList<LocationCatalogEntry> getLocationEntries() {
		return locationEntries;
	}

}
