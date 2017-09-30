package net.vargadaniel.reportengine.reportrenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.vargadaniel.reportengine.reportrenderer.JasperRenderer.ReportFormat;

@RestController
public class RendererController {
	
	final static Logger logger = LoggerFactory.getLogger(RendererController.class);
	
	static byte[] reportTemplate;
	
	static {
	    try (InputStream jrxml = RendererController.class.getResourceAsStream("/dummyReport.jrxml")) {
	        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	        JasperCompileManager.compileReportToStream(jrxml, byteArrayOutputStream);
	        reportTemplate = byteArrayOutputStream.toByteArray();
	    } catch (IOException | JRException e) {
	    		logger.error("Cloud not load report tempalte", e);
	    		System.exit(-1);
		}
	}	
	
	@Autowired
	private OAuth2ProtectedResourceDetails details;
	
	@Bean
	public OAuth2RestTemplate oauth2RestTemplate() {
		return new OAuth2RestTemplate(details);
	}
	
	@Autowired
	OAuth2RestTemplate restTemplate;

	@RequestMapping(path="/files/{id}.pdf", produces="application/pdf")
	public ResponseEntity<byte[]> renderPdfReport(@PathVariable("id") String reportFileId) throws JRException {
		try {
			ResponseEntity<String> reportFileResponse = restTemplate.getForEntity("http://report-repository:8080/files/" + reportFileId, String.class);
			String xmlReport = reportFileResponse.getBody();
			byte[] renderedReport = JasperRenderer.generateReportFile(reportTemplate, xmlReport.getBytes(), ReportFormat.PDF);
			return new ResponseEntity<byte[]>(renderedReport, HttpStatus.OK);
		} catch (HttpClientErrorException hce) {
			if (HttpStatus.NOT_FOUND.equals(hce.getStatusCode())) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			} 
			throw hce;
		}
	}

}
