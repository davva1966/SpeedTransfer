package com.ss.speedtransfer.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.ss.speedtransfer.SpeedTransferPlugin;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.model.SQLScratchPad;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.server.ServerFolderSelectionDialog;


public class ServerExporter {

	protected Object[] itemsToExport;
	protected boolean cancelled = false;
	FormToolkit toolkit;
	Form form;

	protected Text serverAddressEdit;
	protected Text serverPortEdit;
	protected Text proxyAddressEdit;
	protected Text proxyPortEdit;
	protected Text folderEdit;
	protected Button replaceCheck;
	protected Button browseButton;
	protected Button okButton;

	protected String address = "";
	protected int port = 0;
	protected String proxyAddress = "";
	protected int proxyPort = 0;
	protected String folder = "";
	protected String replace = "";

	protected Stack<String> folderStack = new Stack<String>();
	protected Map<String, Exception> errorMap = new HashMap<String, Exception>();
	protected int successCount = 0;

	protected static String WEB_CONTEXT = "proto/speedview";

	static {
		String prop = System.getProperty("com.ss.speedtransfer.webContext");
		if (prop != null)
			WEB_CONTEXT = prop.trim();
	}

	public ServerExporter(Object[] itemsToExport) {
		super();
		this.itemsToExport = itemsToExport;
	}

	public void export() {
		openDialog();
		if (cancelled)
			return;

		try {
			sendItemsToServer();
			UIHelper.instance().showMessage("Information", buildStatusMessage());
		} catch (Exception e) {
			UIHelper.instance().showMessage("Error", "Failed to transmit report to server. Error: " + SSUtil.getMessage(e));
		}

	}

	protected void openDialog() {

		IDialogSettings tempDialogSettings = null;
		try {
			tempDialogSettings = SpeedTransferPlugin.getDialogSettingsFor(this.getClass().getName());
		} catch (Exception e) {
		}
		final IDialogSettings dialogSettings = tempDialogSettings;

		Shell shell = UIHelper.instance().getActiveShell();
		final Shell settingsShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		settingsShell.setText("Export to server");
		Point location = shell.getLocation();
		settingsShell.setBounds(location.x + 30, location.y + 150, 500, 300);
		settingsShell.setLayout(new FillLayout());

		toolkit = new FormToolkit(settingsShell.getDisplay());
		form = toolkit.createForm(settingsShell);

		try {
			form.setBackgroundImage(UIHelper.instance().getImageDescriptor("form_banner.gif").createImage());
		} catch (Exception e) {
		}

		form.setText("Export to server");
		toolkit.decorateFormHeading(form);

		GridLayout layout = new GridLayout(3, false);
		form.getBody().setLayout(layout);

		GridData gd = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 3, 1);

		Group serverGroup = new Group(form.getBody(), SWT.NONE);
		serverGroup.setLayout(new GridLayout(4, false));
		serverGroup.setLayoutData(gd);
		toolkit.adapt(serverGroup);

		// Export to server
		Label label = toolkit.createLabel(serverGroup, "Server:", SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		label.setLayoutData(gd);

		// Export to server entry
		serverAddressEdit = toolkit.createText(serverGroup, "", SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		serverAddressEdit.setLayoutData(gd);
		serverAddressEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});

		// Export to server port
		label = toolkit.createLabel(serverGroup, "Port:", SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		label.setLayoutData(gd);

		// Export to server entry
		serverPortEdit = toolkit.createText(serverGroup, "", SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		serverPortEdit.setLayoutData(gd);
		serverPortEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});

		// Export to server
		label = toolkit.createLabel(serverGroup, "Proxy:", SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		label.setLayoutData(gd);

		// Export to server entry
		proxyAddressEdit = toolkit.createText(serverGroup, "", SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		proxyAddressEdit.setLayoutData(gd);

		// Export to server port
		label = toolkit.createLabel(serverGroup, "Port:", SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		label.setLayoutData(gd);

		// Export to server entry
		proxyPortEdit = toolkit.createText(serverGroup, "", SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		proxyPortEdit.setLayoutData(gd);

		gd = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 3, 1);

		Group group = new Group(form.getBody(), SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(gd);
		toolkit.adapt(group);

		// Export to folder
		label = toolkit.createLabel(group, "Select folder to export to (leave blank for root folder)", SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		label.setLayoutData(gd);

		// Export to folder entry
		folderEdit = toolkit.createText(group, "", SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		folderEdit.setLayoutData(gd);

		// Folder browse button
		browseButton = toolkit.createButton(group, "Browse...", SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 90;
		gd.heightHint = 22;
		browseButton.setLayoutData(gd);
		browseButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				selectFolder();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Replace existing file check box
		replaceCheck = toolkit.createButton(group, "Replace existing report", SWT.CHECK);
		replaceCheck.setSelection(true);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		gd.horizontalIndent = 5;
		gd.heightHint = 25;
		replaceCheck.setLayoutData(gd);

		// OK button
		okButton = toolkit.createButton(form.getBody(), "OK", SWT.PUSH);
		okButton.setEnabled(false);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		okButton.setLayoutData(gd);
		okButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				address = serverAddressEdit.getText();
				port = Integer.parseInt(serverPortEdit.getText());
				proxyAddress = proxyAddressEdit.getText();
				try {
					proxyPort = Integer.parseInt(proxyPortEdit.getText());
				} catch (Exception e2) {
				}
				folder = folderEdit.getText();
				replace = Boolean.toString(replaceCheck.getSelection());

				if (dialogSettings != null) {
					dialogSettings.put("server", serverAddressEdit.getText());
					dialogSettings.put("port", serverPortEdit.getText());
					dialogSettings.put("proxy", proxyAddressEdit.getText());
					dialogSettings.put("proxyport", proxyPortEdit.getText());
					dialogSettings.put("folder", folderEdit.getText());
					dialogSettings.put("replace", replaceCheck.getSelection());
				}

				settingsShell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Cancel button
		Button cancelButton = toolkit.createButton(form.getBody(), "Cancel", SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		cancelButton.setLayoutData(gd);
		cancelButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				cancelled = true;
				settingsShell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		if (dialogSettings != null) {
			if (dialogSettings.get("server") != null)
				serverAddressEdit.setText(dialogSettings.get("server"));
			if (dialogSettings.get("port") != null)
				serverPortEdit.setText(dialogSettings.get("port"));
			if (dialogSettings.get("proxy") != null)
				proxyAddressEdit.setText(dialogSettings.get("proxy"));
			if (dialogSettings.get("proxyport") != null)
				proxyPortEdit.setText(dialogSettings.get("proxyport"));
			if (dialogSettings.get("folder") != null)
				folderEdit.setText(dialogSettings.get("folder"));
			if (dialogSettings.get("replace") != null)
				replaceCheck.setSelection(dialogSettings.getBoolean("replace"));
		}

		settingsShell.setDefaultButton(okButton);

		validatePage();

		// Open shell
		settingsShell.open();
		Display display = Display.getDefault();
		while (!settingsShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

	protected void selectFolder() {

		Document document = null;
		try {
			document = getFolderStructureFromServer();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Error when retrieving folder structure from server. Error: " + SSUtil.getMessage(e));
			return;
		}

		IWorkbenchWindow window = UIHelper.instance().getActiveWindow();
		if (window != null) {
			ServerFolderSelectionDialog dialog = new ServerFolderSelectionDialog(window, document);
			dialog.open();
			if (dialog.getSelectedFolder() != null) {
				String folder = dialog.getSelectedFolder();
				folderEdit.setText(folder);
			}
		}

	}

	protected Document getFolderStructureFromServer() throws Exception {

		String address = serverAddressEdit.getText();
		int port = Integer.parseInt(serverPortEdit.getText());

		String proxyadress = proxyAddressEdit.getText();
		int proxyport = 0;
		try {
			proxyport = Integer.parseInt(proxyPortEdit.getText());
		} catch (Exception e) {
		}

		URI uri = URIUtils.createURI("http", address, port, "/" + WEB_CONTEXT + "/GetFolderStructure", null, null);
		HttpGet httpget = new HttpGet(uri);

		HttpClient httpclient = new DefaultHttpClient();

		// Set proxy if selected
		if (proxyadress.trim().length() > 0) {
			HttpHost proxy = new HttpHost(proxyAddress, proxyport);
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}

		HttpResponse response = httpclient.execute(httpget);
		return processFolderStructureResponse(response);

	}

	protected Document processFolderStructureResponse(HttpResponse response) throws Exception {

		if (response.getStatusLine().getStatusCode() >= 400)
			throw new Exception(response.getStatusLine().toString());

		HttpEntity entity = response.getEntity();
		if (entity == null)
			return null;

		byte[] bytes = getBytes(entity.getContent());
		String resp = new String(bytes);
		if (resp.startsWith("ERROR")) {
			String error = resp.substring(5);
			throw new Exception(error);
		}

		Document document = null;
		InputSource input = new InputSource(new ByteArrayInputStream(bytes));
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		document = dBuilder.parse(input);

		return document;

	}

	protected void sendItemsToServer() throws Exception {

		for (Object item : itemsToExport) {
			if (item instanceof IContainer)
				sendFolderToServer((IContainer) item);
			if (item instanceof IFile)
				sendFileToServer((IFile) item);
		}

	}

	protected void sendFolderToServer(IContainer container) throws Exception {
		folderStack.push(container.getFullPath().lastSegment());
		for (IResource resource : container.members()) {
			if (resource instanceof IContainer)
				sendFolderToServer((IContainer) resource);
			if (resource instanceof IFile)
				sendFileToServer((IFile) resource);
		}
		folderStack.pop();

	}

	protected void sendFileToServer(IFile file) {

		try {

			QueryDefinition queryDef = QueryDefinition.getModelFor(file);

			if (queryDef == null || queryDef instanceof SQLScratchPad)
				return;

			queryDef.embedConnection();

			File f = new File(queryDef.getFile().getLocationURI());
			String serverFilePath = adjustPath(f.getName());

			ByteArrayEntity entity = new ByteArrayEntity(getBytes(queryDef.getInputStream()));

			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			qparams.add(new BasicNameValuePair("reportpath", serverFilePath));
			qparams.add(new BasicNameValuePair("replace", replace));

			String context = "";
			if (WEB_CONTEXT != null && WEB_CONTEXT.trim().length() > 0)
				context = "/" + WEB_CONTEXT + "/ReceiveReport";
			else
				context = "/ReceiveReport";
			URI uri = URIUtils.createURI("http", address, port, context, URLEncodedUtils.format(qparams, "UTF-8"), null);

			HttpPost httppost = new HttpPost(uri);
			httppost.setEntity(entity);

			DefaultHttpClient httpclient = new DefaultHttpClient();

			// Set proxy if selected
			if (proxyAddress.trim().length() > 0) {
				HttpHost proxy = new HttpHost(proxyAddress, proxyPort);
				httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			}

			HttpResponse response = httpclient.execute(httppost);

			processReportTransmit(response);

			successCount++;

		} catch (Exception e) {
			errorMap.put(file.getName(), e);
		}

	}

	protected String adjustPath(String filePath) {
		String newPath = "";
		for (String folder : folderStack) {
			if (newPath.trim().length() > 0 && newPath.trim().endsWith(File.separator) == false)
				newPath = newPath + File.separator;
			newPath = newPath + folder;
		}

		if (folder.endsWith(File.separator) == false)
			folder = folder + File.separator;

		if (newPath.trim().length() > 0)
			newPath = folder + newPath + File.separator + filePath;
		else
			newPath = folder + filePath;

		return newPath;
	}

	protected void processReportTransmit(HttpResponse response) throws Exception {

		if (response.getStatusLine().getStatusCode() >= 400)
			throw new Exception(response.getStatusLine().toString());

		HttpEntity entity = response.getEntity();

		byte[] bytes = getBytes(entity.getContent());
		String resp = new String(bytes);
		if (resp.startsWith("ERROR")) {
			String error = resp.substring(5);
			throw new Exception(error);
		}

	}

	protected void validatePage() {

		okButton.setEnabled(true);
		if (serverAddressEdit.getText().trim().length() == 0)
			okButton.setEnabled(false);
		if (serverPortEdit.getText().trim().length() == 0)
			okButton.setEnabled(false);

		browseButton.setEnabled(false);
		if (serverAddressEdit.getText().trim().length() > 0 && serverPortEdit.getText().trim().length() > 0)
			browseButton.setEnabled(true);

		clearMessage(form, "Server empty", serverAddressEdit);
		if (serverAddressEdit.getText().trim().length() == 0)
			issueMessage(form, "Server empty", IMessageProvider.INFORMATION, "Server address/IP must be specified", serverAddressEdit);

		clearMessage(form, "Port empty", serverPortEdit);
		if (serverPortEdit.getText().trim().length() == 0)
			issueMessage(form, "Port empty", IMessageProvider.INFORMATION, "Server port must be specified", serverPortEdit);

	}

	protected void issueMessage(Form form, String key, int type, String message, Control control) {
		if (control == null)
			form.getMessageManager().addMessage(key, message, null, type);
		else
			form.getMessageManager().addMessage(key, message, null, type, control);
	}

	protected void clearMessage(Form form, String key, Control control) {
		if (control == null)
			form.getMessageManager().removeMessage(key);
		else
			form.getMessageManager().removeMessage(key, control);
	}

	protected byte[] getBytes(InputStream stream) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			byte[] b = new byte[2048];
			int bytes = 0;
			while ((bytes = stream.read(b)) > 0) {
				out.write(b, 0, bytes);
			}
		} finally {
			try {
				stream.close();
			} catch (Exception e) {
			}
			try {
				out.close();
			} catch (Exception e) {
			}
		}

		return out.toByteArray();

	}

	protected String buildStatusMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append(successCount);
		sb.append(" report(s) successfully transmitted to server");
		if (errorMap.size() > 0) {

			sb.append(StringHelper.getNewLine());
			sb.append(errorMap.size());
			sb.append(" report(s) not transmited to server");

			sb.append(StringHelper.getNewLine());
			sb.append(StringHelper.getNewLine());
			sb.append("Error report:");
			sb.append(StringHelper.getNewLine());
			for (String name : errorMap.keySet()) {
				sb.append(name);
				sb.append(" - ");
				sb.append(SSUtil.getMessage(errorMap.get(name)));
				sb.append(StringHelper.getNewLine());
			}
		}

		return sb.toString();
	}

}
