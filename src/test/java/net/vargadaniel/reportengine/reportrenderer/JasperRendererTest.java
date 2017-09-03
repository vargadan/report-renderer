package net.vargadaniel.reportengine.reportrenderer;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.vargadaniel.reportengine.reportrenderer.JasperRenderer.ReportFormat;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertNotNull;

public class JasperRendererTest {

    @Test
    public void testGenerateReportFile1() throws IOException, JRException {
        byte[] report = generateReport(ReportFormat.XLSX);
        saveToFile(report, "target/dummyReport.xlsx");
    }

    @Test
    public void testGenerateReportFile2() throws IOException, JRException {
        byte[] report = generateReport(ReportFormat.DOCX);
        saveToFile(report, "target/dummyReport.docx");
    }

    @Test
    public void testGenerateReportFile3() throws IOException, JRException {
        byte[] report = generateReport(ReportFormat.PDF);
        saveToFile(report, "target/dummyReport.pdf");
    }

    private void saveToFile(byte[] report, String name) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(name)) {
            fileOutputStream.write(report);
            fileOutputStream.flush();
        }
    }

    private byte[] generateReport(ReportFormat format) throws IOException, JRException {
        try (InputStream jrxml = getClass().getResourceAsStream("/dummyReport.jrxml")) {
            byte[] source = getSource("/xml/dummyReport.xml");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JasperCompileManager.compileReportToStream(jrxml, byteArrayOutputStream);
            byte[] report = JasperRenderer.generateReportFile(byteArrayOutputStream.toByteArray(), source, format);
            assertNotNull(report);
            return report;
        }
    }

    private byte[] getSource(String name) throws IOException {
        try (InputStream dataStream = getClass().getResourceAsStream(name);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(dataStream)) {
            int i;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while ((i = bufferedInputStream.read()) != -1) {
                byteArrayOutputStream.write(i);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }
}
