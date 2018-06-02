package se.tink.backend.system.cli;

import java.io.IOException;
import java.io.InputStream;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

public class ExcelUtils {
	public static Workbook openWorkbook(InputStream stream)
			throws BiffException, IOException {
		WorkbookSettings ws = new WorkbookSettings();
		ws.setEncoding("8859_1");

		Workbook workbook = Workbook.getWorkbook(stream, ws);

		return workbook;
	}
}
