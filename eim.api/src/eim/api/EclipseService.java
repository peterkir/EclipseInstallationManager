package eim.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface EclipseService {
	Process startProcess(String command, String workingDir, String[] args);
}
