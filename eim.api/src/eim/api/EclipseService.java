package eim.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface EclipseService {
	void startProcess(String command, String workingDir, String[] args);
}
