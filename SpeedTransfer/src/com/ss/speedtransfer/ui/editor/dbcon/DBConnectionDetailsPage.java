package com.ss.speedtransfer.ui.editor.dbcon;

import java.sql.Connection;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.UIHelper;


public class DBConnectionDetailsPage extends AbstractDBConnectionDetailsPage {

	protected String PAGE_HEADING = "";
	protected String PAGE_DESCRIPTION = "";

	protected Composite baseSection;
	protected Combo typeCombo;
	protected Text hostName;
	protected Button browseButton;
	protected Text port;
	protected Label hostLabel;
	protected Label portLabel;
	protected Label databaseLabel;
	protected Text database;
	protected Composite logonComp;
	protected Text user;
	protected Text password;
	protected Button promptButton;
	protected Button showURIButton;
	protected Button validateButton;

	protected boolean hasError = false;

	public DBConnectionDetailsPage(DBConnection model) {
		super(model);
	}

	public void createContents(Composite parent) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 10;
		layout.leftMargin = 5;
		layout.rightMargin = 2;
		layout.bottomMargin = 2;
		parent.setLayout(layout);

		FormToolkit toolkit = mform.getToolkit();
		Section s1 = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		s1.marginWidth = 10;
		s1.setText("Database Connection");
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		s1.setLayoutData(td);

		baseSection = toolkit.createComposite(s1);
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = toolkit.getBorderStyle() == SWT.BORDER ? 0 : 2;
		glayout.numColumns = 2;
		glayout.marginBottom = 10;
		glayout.verticalSpacing = 8;
		baseSection.setLayout(glayout);

		createControls(parent);

		toolkit.paintBordersFor(baseSection);
		s1.setClient(baseSection);
	}

	protected void createControls(Composite parent) {

		FormToolkit toolkit = mform.getToolkit();

		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 25;
		gd.heightHint = 15;
		gd.horizontalIndent = 7;

		toolkit.createLabel(baseSection, "Connection Type");
		String[] types = new String[7];
		types[0] = DBConnection.ISERIES;
		types[1] = DBConnection.MYSQL;
		types[2] = DBConnection.SQLSERVER;
		types[3] = DBConnection.ORACLE;
		types[4] = DBConnection.POSTGRESQL;
		types[5] = DBConnection.DB2;
		types[6] = DBConnection.CSV;
		typeCombo = createDropDown(baseSection, types, 0);
		typeCombo.setLayoutData(gd);
		typeCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				String oldValue = getAttributeValue(DBConnection.TYPE);
				setAttributeValue(DBConnection.TYPE, typeCombo.getText());
				String newValue = getAttributeValue(DBConnection.TYPE);
				clearMessage("type changed warning", typeCombo);
				if (oldValue.equals(newValue) == false && getDBCon().hasConnectionProperties())
					issueMessage("type changed warning", IMessageProvider.WARNING,
							"You have changed the connection type and have connection properties defined. The connection properties may no longer be valid.", typeCombo);
				if (oldValue.equals(newValue) == false && getDBCon().getConnectionType().equalsIgnoreCase(DBConnection.CSV))
					UIHelper.instance()
							.showMessage(
									"Information",
									"Note!"
											+ StringHelper.getNewLine()
											+ "The CSV connection type only has limited sql support. Only simple SELECT statements are supported (No join, grouping or other advanced functions are supported)");
				validate();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		hostLabel = toolkit.createLabel(baseSection, "Host/Adress ");

		GridLayout hostlayout = new GridLayout();
		hostlayout.numColumns = 2;

		GridData compGD = new GridData();
		compGD.grabExcessHorizontalSpace = true;
		compGD.horizontalAlignment = SWT.FILL;

		Composite hostComp = toolkit.createComposite(baseSection, SWT.NONE);
		hostComp.setLayout(hostlayout);
		hostComp.setLayoutData(compGD);

		GridData hostGD = new GridData();
		hostGD.grabExcessHorizontalSpace = true;
		hostGD.horizontalAlignment = SWT.FILL;

		hostName = toolkit.createText(hostComp, "", SWT.SINGLE);
		hostName.setLayoutData(hostGD);
		hostName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setAttributeValue(DBConnection.HOST, hostName.getText());
				validate();
			}
		});

		// Directory browse button
		browseButton = toolkit.createButton(hostComp, "Browse...", SWT.PUSH);
		browseButton.setVisible(false);
		browseButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				String dir = UIHelper.instance().selectDirectory(null, "Select directory for csv files");
				if (dir != null)
					hostName.setText(dir);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		portLabel = toolkit.createLabel(baseSection, "Port ");
		port = toolkit.createText(baseSection, "", SWT.SINGLE);
		port.setLayoutData(gd);
		port.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String p = port.getText();
				if (p.trim().length() == 0)
					p = "0";
				setAttributeValue(DBConnection.PORT, p);
				validate();
			}
		});

		databaseLabel = toolkit.createLabel(baseSection, "Database       ");
		database = toolkit.createText(baseSection, "", SWT.SINGLE);
		database.setLayoutData(gd);
		database.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setAttributeValue(DBConnection.DATABASE, database.getText());
			}
		});

		GridData gd1 = new GridData();
		gd1.horizontalSpan = 2;
		gd1.grabExcessHorizontalSpace = true;
		gd1.horizontalAlignment = SWT.FILL;

		GridLayout glayout = new GridLayout();
		glayout.numColumns = 2;

		logonComp = toolkit.createComposite(baseSection, SWT.BORDER);
		logonComp.setLayoutData(gd1);
		logonComp.setLayout(glayout);

		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 25;
		gd.heightHint = 15;
		gd.horizontalIndent = 7;

		toolkit.createLabel(logonComp, "User ");
		user = toolkit.createText(logonComp, "", SWT.SINGLE);
		user.setLayoutData(gd);
		user.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setAttributeValue(DBConnection.USER, user.getText());
				validate();
			}
		});

		toolkit.createLabel(logonComp, "Password");
		password = toolkit.createText(logonComp, "", SWT.SINGLE | SWT.PASSWORD);
		password.setLayoutData(gd);
		password.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setAttributeValueEncrypted(DBConnection.PASSWORD, password.getText());
				validate();
			}
		});

		promptButton = toolkit.createButton(logonComp, "Prompt for user/pwd at runtime", SWT.CHECK);
		promptButton.setLayoutData(gd1);
		promptButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setAttributeValue(DBConnection.PROMPT, promptButton.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		validateButton = toolkit.createButton(baseSection, "Validate Connection", SWT.PUSH);
		validateButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				validateConnection();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		showURIButton = toolkit.createButton(baseSection, "Show Connection Info", SWT.PUSH);
		showURIButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				showConnectionString();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	public void validateConnection() {
		Connection con = null;
		try {
			getDBCon().reset();
			con = ConnectionManager.getConnection(getDBCon());
			if (con != null) {
				UIHelper.instance().showMessage("Success!", "Connection is valid");
				return;
			}

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Connection is not valid. Error: " + SSUtil.getMessage(e));
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (Exception e2) {
			}

		}

	}

	public void showConnectionString() {
		try {
			String connString = ConnectionManager.buildConnectionInfoString(getDBCon());
			UIHelper.instance().showMessage("Connection info", connString);
			return;

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}

	}

	protected void internalUpdate() {
		updateCombo(typeCombo, getAttributeValue(DBConnection.TYPE));
		updateText(hostName, getAttributeValue(DBConnection.HOST));
		updateText(port, getAttributeValue(DBConnection.PORT));
		updateText(database, getAttributeValue(DBConnection.DATABASE));
		updateText(user, getAttributeValue(DBConnection.USER));
		updateTextEncrypted(password, getAttributeValue(DBConnection.PASSWORD));
		updateBoolean(promptButton, getAttributeValueBoolean(DBConnection.PROMPT));
		validatePage();
	}

	public void setFocus() {
		typeCombo.setFocus();
	}

	protected void validatePage() {

		hasError = false;

		if (typeCombo.getText().equals(DBConnection.CSV)) {
			hostLabel.setText("Directory  ");
			browseButton.setVisible(true);
			portLabel.setVisible(false);
			port.setVisible(false);
			databaseLabel.setVisible(false);
			database.setVisible(false);
			promptButton.setSelection(false);
			logonComp.setVisible(false);
		} else {
			hostLabel.setText("Host/Adress ");
			browseButton.setVisible(false);
			portLabel.setVisible(true);
			port.setVisible(true);
			databaseLabel.setVisible(true);
			database.setVisible(true);
			logonComp.setVisible(true);

			if (typeCombo.getText().equals(DBConnection.ISERIES))
				databaseLabel.setText("Library          ");
			else if (typeCombo.getText().equals(DBConnection.ORACLE))
				databaseLabel.setText("Service/SID   ");
			else
				databaseLabel.setText("Database         ");
		}

		clearMessage("Host empty", hostName);
		if (hostName.getText().trim().length() == 0) {
			if (typeCombo.getText().equals(DBConnection.CSV))
				issueMessage("Host empty", IMessageProvider.ERROR, "Directory must be specified", hostName);
			else
				issueMessage("Host empty", IMessageProvider.ERROR, "Host must be specified", hostName);
			hasError = true;
		}

		clearMessage("Port invalid", port);
		clearMessage("User empty", user);
		clearMessage("Password empty", password);
		boolean error = false;

		if (typeCombo.getText().equals(DBConnection.CSV) == false) {
			if (port.getText().trim().length() > 0) {
				int p = 0;
				try {
					p = Integer.parseInt(port.getText().trim());
					if (p < 0 && p > 65535)
						error = true;
				} catch (NumberFormatException e) {
					error = true;
				}
				if (error) {
					issueMessage("Port invalid", IMessageProvider.ERROR, "Port number must be an integer between 0 and 65535.", port);
					hasError = true;
				}
			}

			if (promptButton.getSelection() == false) {
				if (user.getText().trim().length() == 0) {
					issueMessage("User empty", IMessageProvider.ERROR, "User must be specified", user);
					hasError = true;
				}
				if (password.getText().trim().length() == 0) {
					issueMessage("Password empty", IMessageProvider.ERROR, "Password must be specified", password);
					hasError = true;
				}
			}
		}

		validateButton.setEnabled(hasError == false);
		baseSection.layout();

	}

}
