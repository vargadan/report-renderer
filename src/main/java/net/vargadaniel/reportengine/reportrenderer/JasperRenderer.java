package net.vargadaniel.reportengine.reportrenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRTextField;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

public final class JasperRenderer {

    public enum ReportFormat {
        XLSX,
        DOCX,
        PDF
    }

    public static byte[] generateReportFile(byte[] reportTemplate, byte[] sourceData,
                                            ReportFormat reportFormat) throws JRException {
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(new ByteArrayInputStream(reportTemplate));
        if (jasperReport == null) {
            throw new JRException("Cannot build report from stream");
        }

        jasperReport.setProperty(JRTextField.PROPERTY_PRINT_KEEP_FULL_TEXT, "true");
        Map<String, Object> params = new HashMap<>();
        params.put(JRXPathQueryExecuterFactory.XML_INPUT_STREAM, new ByteArrayInputStream(sourceData));
        JRXmlDataSource source = new JRXmlDataSource(new ByteArrayInputStream(sourceData));
        source.setDatePattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, source);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRAbstractExporter<?, ?, OutputStreamExporterOutput, ?> exporter = getExporter(reportFormat);
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        //noinspection unchecked
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        exporter.exportReport();
        return outputStream.toByteArray();
    }

    private static JRAbstractExporter<?, ?, OutputStreamExporterOutput, ?> getExporter(ReportFormat reportFormat) {
        switch (reportFormat) {
            case DOCX: {
                return new JRDocxExporter();
            }
            case XLSX: {
                return new JRXlsxExporter();
            }
            case PDF: {
                JRPdfExporter exporter = new JRPdfExporter();
                SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
                configuration.setCompressed(true);
                exporter.setConfiguration(configuration);
                return exporter;
            }
            default:
                throw new IllegalArgumentException("Unknown export type " + reportFormat);

        }
    }

    private JasperRenderer() {
    }
}