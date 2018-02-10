package net.vargadaniel.reportengine.reportrenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

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
	
    @Bean
    public OAuth2RestTemplate oauth2RestTemplate(OAuth2ClientContext oauth2ClientContext) {
    		OAuth2ProtectedResourceDetails details = new ClientCredentialsResourceDetails();
        return new OAuth2RestTemplate(details, oauth2ClientContext);
    }
	
	@Autowired
	OAuth2RestTemplate restTemplate;

	@RequestMapping(path="/files/{id}.pdf", produces="application/pdf")
	public ResponseEntity<byte[]> renderPdfReport(@PathVariable("id") String reportFileId, @AuthenticationPrincipal Principal principal) throws JRException {
		logger.info("calling /files/{}.pdf with princiapl:{}", reportFileId, principal.getName());
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