package eim;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(property = { "osgi.command.scope:String=zEMI",
		"osgi.command.function:String=startProcess" }, service = EclipseStarterCommand.class)
public class EclipseStarterCommand {

	private EclipseService eclService;

	@Reference
	public void bindEclipseService(EclipseService eclsvc) {
		this.eclService = eclsvc;
	}

	public void startProcess(String command, String workingDir, String[] args) {
		System.out.println("Executing " + command);
		eclService.startProcess(command, workingDir, args);
	}
}
