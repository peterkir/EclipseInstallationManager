package eclipse_installation_manager;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(property= {"osgi.command.scope:String=EMI",
					  "osgi.command.function:String=startProcess"},
		service=EclipseStarterCommand.class
)
public class EclipseStarterCommand {
	
	private EclipseService eclService;
	
	@Reference
	void bindEclipseService(EclipseService eclService) {
		this.eclService = eclService;
	}
	
	public void startProcess(String command, String workingDir, String[] args) {
		eclService.startProcess(command, workingDir, args);
	}
}
