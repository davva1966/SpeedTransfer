package com.ss.speedtransfer.ui.editor.querydef;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Element;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.UIHelper;


public class QueryDefinitionLabelProvider extends LabelProvider {

	public String getText(Object obj) {
		String text = "Unknown";
		if (obj instanceof Element) {
			Element elem = (Element) obj;
			try {
				if (elem.getTagName().equalsIgnoreCase(QueryDefinition.QUERY_DEFINITION))
					text = "Query Definition";
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.SQL))
					text = "SQL";
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.REPLACEMENT_VAR)) {
					text = "Replacement variable";
					text = text + " (" + elem.getAttribute(QueryDefinition.NAME) + ")";
				} else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.EXECUTION))
					text = "Execution parameters";
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.COMMENT))
					text = "Comments";
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.DEFAULTS))
					text = "Saved defaults";
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.EXCEL_DEFAULTS))
					text = "Excel export defaults";
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.PDF_DEFAULTS))
					text = "PDF export defaults";
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.CSV_DEFAULTS))
					text = "CSV export defaults";
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.SELECTION_DEFAULTS))
					text = "Selection defaults";
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.DATABASE_DEFAULTS))
					text = "Database export defaults";
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.SELECTION))
					text = "Selection";

			} catch (Exception e) {
			}
		}

		if (text == null || text.trim().length() == 0)
			text = obj.toString();

		return text;
	}

	public Image getImage(Object obj) {
		Image image = null;
		if (obj instanceof Element) {
			Element elem = (Element) obj;
			try {
				if (elem.getTagName().equalsIgnoreCase(QueryDefinition.QUERY_DEFINITION))
					image = UIHelper.instance().getImage("query_def.gif"); 
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.SQL))
					image = UIHelper.instance().getImage("sql_editor.gif"); 
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.REPLACEMENT_VAR))
					image = UIHelper.instance().getImage("variable.gif"); 
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.EXECUTION))
					image = UIHelper.instance().getImage("execute.gif"); 
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.COMMENT))
					image = UIHelper.instance().getImage("help.gif"); 
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.DEFAULTS))
					image = UIHelper.instance().getImage("defaults.gif"); 
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.EXCEL_DEFAULTS))
					image = UIHelper.instance().getImage("excel.gif"); 
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.PDF_DEFAULTS))
					image = UIHelper.instance().getImage("pdf.gif"); 
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.CSV_DEFAULTS))
					image = UIHelper.instance().getImage("csv.gif"); 
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.SELECTION_DEFAULTS))
					image = UIHelper.instance().getImage("selections.gif"); 
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.DATABASE_DEFAULTS))
					image = UIHelper.instance().getImage("export_to_db.gif"); 
				else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.SELECTION))
					image = UIHelper.instance().getImage("variable.gif"); 
			} catch (Exception e) {
			}
		}

		if (image == null)
			image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);

		return image;
	}
}
