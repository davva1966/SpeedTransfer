package test;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ReadExcel {

	public static void main(String[] args) {

		try {

			String sheetName = "NyFlik";
			Workbook wb = new XSSFWorkbook(new FileInputStream("C:\\Temp2\\test.xlsx"));

			for (int k = 0; k < wb.getNumberOfSheets(); k++) {
				Sheet sheet = wb.getSheetAt(k);
				System.out.println("Sheet " + k + " \"" + wb.getSheetName(k));
			}

			Sheet dataSheet = wb.getSheet(sheetName);
			if (dataSheet == null)
				dataSheet = wb.createSheet(sheetName);

			Row row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
			Cell cell = row.createCell(0);
			cell.setCellValue("Column1");
			cell = row.createCell(1);
			cell.setCellValue("Column2");

			row = dataSheet.createRow(dataSheet.getLastRowNum() + 1);
			cell = row.createCell(0);
			cell.setCellValue("Data1");
			cell = row.createCell(1);
			cell.setCellValue("Data2");

			FileOutputStream fileOut = new FileOutputStream("C:\\Temp2\\test.xlsx");
			wb.write(fileOut);
			fileOut.close();
			fileOut = null;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
