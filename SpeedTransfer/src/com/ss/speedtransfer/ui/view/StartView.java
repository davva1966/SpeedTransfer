package com.ss.speedtransfer.ui.view;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.themes.IThemeManager;

import com.ss.speedtransfer.SpeedTransferPlugin;
import com.ss.speedtransfer.util.LicenseManager;


/**
 * Displays messages
 * 
 */
public class StartView extends ViewPart implements PaintListener {

	public static final String ID = "com.ss.speedtransfer.startView";

	protected Composite paintCanvas = null;

	protected Color bottomColor = null;
	protected Color topColor = null;

	protected Color viewbarColor = null;

	IPartListener partListener = null;

	protected Image transparentImage = null;

	protected int origoX = 0;
	protected int origoY = 0;
	protected int radius = 120;
	protected int endX = 0;
	protected int endY = 0;
	protected double test = 0;
	protected int maxX = 0;
	protected int maxY = 0;

	protected Font font = null;
	
	public void createPartControl(Composite parent) {
		setPartName("Welcome");
		transparentImage = SpeedTransferPlugin.getImageDescriptor("DataTool_128.png").createImage();
		paintCanvas = new Canvas(parent, SWT.NO_FOCUS | SWT.NO_BACKGROUND);

		// Get top- and bottom colors
		bottomColor = new Color(parent.getDisplay(), new RGB(156, 173, 203));
		topColor = new Color(parent.getDisplay(), new RGB(153, 189, 237));

		font = new Font(parent.getDisplay(), "Arial", 30, SWT.BOLD);
		paintCanvas.addPaintListener(this);
//		paintViewBar();

		partListener = new IPartListener() {
			public void partActivated(IWorkbenchPart part) {
				if (getPartName().equals(part.getTitle())) {
//					paintViewBar();
				}
			}

			public void partBroughtToTop(IWorkbenchPart part) {
			}

			public void partClosed(IWorkbenchPart part) {
			}

			public void partDeactivated(IWorkbenchPart part) {
			}

			public void partOpened(IWorkbenchPart part) {
			}
		};
		getSite().getPage().addPartListener(partListener);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		super.dispose();
		if (partListener != null)
			getSite().getPage().removePartListener(partListener);
	}

	public void paintControl(PaintEvent e) {
		if (!paintCanvas.isDisposed()) {
			Image image = new Image(e.display, paintCanvas.getBounds());
			GC gc = new GC(image);
			gc.setBackground(topColor);
			gc.setForeground(bottomColor);
			gc.fillRectangle(0, 0, paintCanvas.getSize().x, paintCanvas.getSize().y);

			gc.setFont(font);
			String text = "SpeedTransfer Developer Studio";
			if (LicenseManager.isBrowserVersion())
				text = "SpeedTransfer Browser";
			int fontCharWidth = gc.getFontMetrics().getAverageCharWidth();
			int start = (paintCanvas.getSize().x - (text.length() * fontCharWidth)) / 2;
			gc.drawText(text, start, 75, false);

			if (LicenseManager.isTrial())
				text = "Trial version, " + LicenseManager.getDaysRemaining() + " days remaining";
			else
				text = "License: " + LicenseManager.getLicense();

			start = (paintCanvas.getSize().x - (text.length() * fontCharWidth)) / 2;
			gc.drawText(text, start, paintCanvas.getSize().y - 75, false);

			if (transparentImage != null)
				gc.drawImage(transparentImage, paintCanvas.getSize().x / 2 - transparentImage.getBounds().width / 2, paintCanvas.getSize().y / 2 - transparentImage.getBounds().height / 2);
			e.gc.drawImage(image, 0, 0);
			image.dispose();
			gc.dispose();
		}
	}

	protected Color getThemeColor(String name) {
		IThemeManager themeManager = getSite().getWorkbenchWindow().getWorkbench().getThemeManager();
		ColorRegistry colorRegistry = themeManager.getCurrentTheme().getColorRegistry();
		return colorRegistry.get(name);
	}

//	private void paintViewBar() {
//		WorkbenchPage p = (WorkbenchPage) getSite().getPage();
//		Color color = new Color(UIHelper.instance().getDisplay(), new RGB(238, 239, 238));
//		Control[] ctrls = p.getClientComposite().getTabList();
//		for (Control ctrl : ctrls) {
//			ctrl.setBackground(color);
//		}
//	}

}
