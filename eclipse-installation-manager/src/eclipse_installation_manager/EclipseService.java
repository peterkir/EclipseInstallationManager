package eclipse_installation_manager;

public interface EclipseService {
	void startProcess(String command, String workingDir, String[] args);
}
