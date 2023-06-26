package tray.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.oomph.setup.Installation;
import org.eclipse.oomph.setup.Workspace;
import org.eclipse.oomph.util.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eim.api.EclipseService;
import eim.api.ListLocationService;

@Component(immediate = true)
public class TrayApplication {

	@Reference
	private EclipseService eclService;
	@Reference
	private ListLocationService listLocSvc;
	private Map<Integer, Pair<Installation, Workspace>> locationEntries;
	private Logger logger = LoggerFactory.getLogger(TrayApplication.class);
	private Map<Installation, List<Pair<Integer, Workspace>>> installationGroupedMap = new HashMap<>();

	public static boolean dispose = false;

	@Activate
	public void activate() {
		dataInitialization();
		createMappedInstallationEntries();
		createDisplay();
	}

	private void dataInitialization() {
		logger.debug("Loading data");
		listLocSvc.listLocations(null);
		locationEntries = listLocSvc.getLocationEntries();
	}

	private void createDisplay() {
		logger.debug("Starting to create UI");
		Display display = new Display();
		Shell shell = new Shell(display);
		Image image = new Image(display, 16, 16);
		Image image2 = new Image(display, 16, 16);
		GC gc = new GC(image2);
		gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_RED));
		gc.fillRectangle(image2.getBounds());
		gc.dispose();
		final Tray tray = display.getSystemTray();
		if (tray == null) {
			logger.error("The system tray is not available!");
		} else {
			final TrayItem item = new TrayItem(tray, SWT.NONE);
			item.setToolTipText("Eclipe Installation Manager");
			item.addListener(SWT.Show, event -> System.out.println("show"));
			item.addListener(SWT.Hide, event -> System.out.println("hide"));
			item.addListener(SWT.Selection, event -> System.out.println("selection"));
			item.addListener(SWT.DefaultSelection, event -> System.out.println("default selection"));
			final Menu menu = new Menu(shell, SWT.POP_UP);

			installationGroupedMap.forEach((installation, workspaceList) -> {
				Path instPath = Paths.get(installation.eResource().getURI().toFileString()).getParent().getParent()
						.getParent().getParent();
				if (workspaceList.size() == 1) {
					MenuItem mi = new MenuItem(menu, SWT.WRAP | SWT.PUSH);
					Path wrkspcPath = Paths.get(workspaceList.get(0).getElement2().eResource().getURI().toFileString())
							.getParent().getParent().getParent().getParent();
					Integer launchNumber = workspaceList.get(0).getElement1();
					String itemLabel = launchNumber + " # " + instPath.toFile().getName() + " # "
							+ wrkspcPath.toFile().getName();
					mi.setText(itemLabel);
					mi.addListener(SWT.Selection, event -> eclService.startEntry(launchNumber, locationEntries));
				} else {
					MenuItem mi = new MenuItem(menu, SWT.CASCADE);
					mi.setText(instPath.toFile().getName());
					Menu subMenu = new Menu(shell, SWT.DROP_DOWN);
					mi.setMenu(subMenu);

					for (Pair<Integer, Workspace> pair : workspaceList) {
						MenuItem subMenuItem = new MenuItem(subMenu, SWT.PUSH);
						Path wrkspcPath = Paths
								.get(pair.getElement2().eResource().getURI().toFileString()).getParent()
								.getParent().getParent().getParent();
						subMenuItem.setText(wrkspcPath.toFile().getName());
						subMenuItem.addListener(SWT.Selection,
								event -> eclService.startEntry(pair.getElement1(), locationEntries));
					}
					mi.addListener(SWT.MouseHover, event -> subMenu.setVisible(true));
				}
			});
			item.addListener(SWT.MenuDetect, event -> menu.setVisible(true));

			// TODO: Add Settings Menu on SWT.MenuDetect for refresh of the catalog
			item.setImage(image2);
			item.setHighlightImage(image);
		}
		logger.debug("Waiting for disposal");
		while (!dispose) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		logger.debug("Disposing and exiting");
		image.dispose();
		image2.dispose();
		display.dispose();
	}

	private void createMappedInstallationEntries() {
		logger.debug("creating installation - workspaces map");

		HashSet<Installation> uniqueInstallations = new HashSet<>();
		locationEntries.forEach((entryNumber, instWorkspcPair) -> {
			Installation installation = instWorkspcPair.getElement1();
			uniqueInstallations.add(installation);
		});

		uniqueInstallations.forEach(installation -> {
			Path installationPath = Paths.get(installation.eResource().getURI().toFileString()).getParent().getParent()
					.getParent().getParent();
			logger.debug("Creating entry for installation " + installationPath);
			LinkedList<Pair<Integer, Workspace>> mappedWorkspaces = new LinkedList<>();
			locationEntries.forEach((entryNumber, instWorkspcPair) -> {
				Path instPathToCompare = Paths.get(instWorkspcPair.getElement1().eResource().getURI().toFileString())
						.getParent().getParent().getParent().getParent();
				if (0 == installationPath.compareTo(instPathToCompare)) {
					Pair<Integer, Workspace> integerWorkspacePair = new Pair<Integer, Workspace>();
					integerWorkspacePair.setElement1(entryNumber);
					integerWorkspacePair.setElement2(instWorkspcPair.getElement2());
					mappedWorkspaces.add(integerWorkspacePair);
				}
				logger.debug("Putting " + installationPath.toFile().getName() + " with Workspaces "
						+ mappedWorkspaces.toString());

				installationGroupedMap.put(installation, mappedWorkspaces);
			});
		});
	}

}
