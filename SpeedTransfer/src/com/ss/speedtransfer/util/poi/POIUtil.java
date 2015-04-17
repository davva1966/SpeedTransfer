package com.ss.speedtransfer.util.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class POIUtil {

	private static class WorkbookHandler extends DefaultHandler {
		protected List<String> sheetNames = new ArrayList<String>();

		private WorkbookHandler() {
		}

		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			if (name.equals("sheet"))
				sheetNames.add(attributes.getValue("name"));
		}

		public void endElement(String uri, String localName, String name) throws SAXException {
		}

		public String[] getSheetNames() {
			return sheetNames.toArray(new String[0]);
		}
	}

	private static class RecordListener implements HSSFListener {
		protected List<String> sheetNames = new ArrayList<String>();

		private RecordListener() {
		}

		public void processRecord(Record record) {
			if (record.getSid() == BoundSheetRecord.sid) {
				BoundSheetRecord bsr = (BoundSheetRecord) record;
				sheetNames.add(bsr.getSheetname());
			}
		}

		public String[] getSheetNames() {
			return sheetNames.toArray(new String[0]);
		}
	}

	public static String[] getAllSheetNames(String filename) throws Exception {
		return getAllSheetNames(new File(filename));

	}

	public static String[] getAllSheetNames(File file) throws Exception {
		if (file.exists() == false)
			return new String[0];

		try {
			return getAllSheetNamesXSSF(file);
		} catch (Exception e) {
			return getAllSheetNamesHSSF(file);
		}

	}

	public static String[] getAllSheetNamesXSSF(File file) throws Exception {
		OPCPackage pkg = OPCPackage.open(file.getAbsolutePath());
		XSSFReader r = new XSSFReader(pkg);

		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		WorkbookHandler handler = new WorkbookHandler();
		parser.setContentHandler(handler);

		InputStream workbookData = r.getWorkbookData();
		try {
			InputSource workbookSource = new InputSource(workbookData);
			parser.parse(workbookSource);
		} catch (Exception e) {
		} finally {
			if (workbookData != null)
				workbookData.close();
		}

		return handler.getSheetNames();

	}

	public static String[] getAllSheetNamesHSSF(File file) throws IOException {

		FileInputStream fin = new FileInputStream(file);
		POIFSFileSystem poifs = new POIFSFileSystem(fin);
		InputStream din = poifs.createDocumentInputStream("Workbook");

		RecordListener listener = new RecordListener();
		try {
			HSSFRequest req = new HSSFRequest();
			req.addListenerForAllRecords(listener);

			HSSFEventFactory factory = new HSSFEventFactory();
			factory.processEvents(req, din);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			fin.close();
			din.close();
		}

		return listener.getSheetNames();

	}

	public void processRecord(Record record) {
		if (record.getSid() == BoundSheetRecord.sid) {
			BoundSheetRecord bsr = (BoundSheetRecord) record;
			System.out.println(bsr.getSheetname());
		}

	}

}
