package eim;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * This class controls all aspects of the application's execution
 */
@Component
public class EclipseServiceImpl implements EclipseService {

	@Override
	public void startProcess(String command, String workingDir, String[] args) {
		ProcessBuilder pb = new ProcessBuilder(command);
		if (workingDir != null) {
			pb.directory(Paths.get(workingDir).toFile());
		}
		Map<String, String> env = pb.environment();
		if (args != null) {
			for (String string : args) {
				String[] argument = string.split("=");
				env.put(argument[0], argument[1]);
			}
		}
		try {
			pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
