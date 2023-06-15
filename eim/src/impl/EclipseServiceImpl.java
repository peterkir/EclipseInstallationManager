package impl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.oomph.setup.Installation;
import org.eclipse.oomph.setup.Workspace;
import org.eclipse.oomph.util.Pair;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eim.api.EclipseService;

/**
 * This class controls all aspects of the application's execution
 */
@Component
public class EclipseServiceImpl implements EclipseService {
	
	private static final Logger logger = LoggerFactory.getLogger(EclipseServiceImpl.class);
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
			logger.error("There was a problem starting the the process!\n"
					+ "Command: " + command + "\n"
					+ e.getMessage());
			e.printStackTrace();
			return null;
		}

	}
	@Override
	public void startEntry(Integer index, Map<Integer, Pair<Installation, Workspace>> executionEntries) {

		if (executionEntries.containsKey(index)) {
			Pair<Installation, Workspace> entry = executionEntries.get(index);
			Path installationPath = Paths.get(entry.getElement1().eResource().getURI().toFileString()).getParent()
					.getParent().getParent();
			logger.debug("Installation path is " + installationPath.toAbsolutePath().toString());
			Path workspacePath = Paths.get(entry.getElement2().eResource().getURI().toFileString()).getParent()
					.getParent().getParent().getParent();
			logger.debug("Workspace path is " + workspacePath.toAbsolutePath().toString());
			System.getProperty("eclipse.launcher.name");
			Path programPath = installationPath.resolve("eclipse.exe");
			ArrayList<String> args = new ArrayList<String>();
			args.add("ws=" + workspacePath.toString());
			String[] simpleArray = new String[args.size()];
			args.toArray(simpleArray);
			
			logger.debug("Starting " + programPath.toString() + " in working directory " + installationPath.toString() + " with arguments\n " + Arrays.toString(simpleArray));
			startProcess(programPath.toString(), installationPath.toString(), simpleArray);
		}
	}

}
