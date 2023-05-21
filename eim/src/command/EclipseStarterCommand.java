package command;


import org.apache.felix.service.command.Descriptor;
import org.osgi.annotation.versioning.ConsumerType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eim.api.EclipseService;

@Component(property = { "osgi.command.scope=zEMI",  
		"osgi.command.function=startProcess" }, service = EclipseStarterCommand.class)
@ConsumerType
public class EclipseStarterCommand {

	private EclipseService eclService;

	@Reference
	public void bindEclipseService(EclipseService eclsvc) {
		this.eclService = eclsvc;
	}
	
	@Descriptor("Start a process with a specific command.")
	public void startProcess(String command, String workingDir, String[] args) {
		System.out.println("Executing " + command);
		if (workingDir == "null") {
			workingDir = null;
		}
		if (args[0] == "null") {
			args = null;
		}
		eclService.startProcess(command, workingDir, args);
	}
}
