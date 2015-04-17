package test;

import java.io.InputStream;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class Test7 {

	public static void main(String[] args) throws Exception {
		Test7 howto = new Test7();
		howto.processAllSheets("D:\\Temp\\testsize.xlsx");
	}

	public void processAllSheets(String filename) throws Exception {
		OPCPackage pkg = OPCPackage.open(filename);
		XSSFReader r = new XSSFReader(pkg);

		XMLReader parser = fetchWorkbookParser();

		InputStream workbookData = r.getWorkbookData();
		InputSource workbookSource = new InputSource(workbookData);
		parser.parse(workbookSource);
		workbookData.close();

	}

	public XMLReader fetchWorkbookParser() throws SAXException {
		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		ContentHandler handler = new WorkbookHandler();
		parser.setContentHandler(handler);
		return parser;
	}

	private static class WorkbookHandler extends DefaultHandler {

		private WorkbookHandler() {
		}

		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			if (name.equals("sheet"))
				System.out.println(attributes.getValue("name"));
		}

		public void endElement(String uri, String localName, String name) throws SAXException {
		}

	}

}
