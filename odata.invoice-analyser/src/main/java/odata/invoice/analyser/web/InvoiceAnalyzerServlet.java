package odata.invoice.analyser.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odata.invoice.analyser.deutschebahn.DBAnalyzer;
import odata.invoice.analyser.service.InvoiceAnalyzerEdmProvider;
import odata.invoice.analyser.service.InvoiceAnalyzerProcessor;
import odata.invoice.analyser.zvv.ZVVAnalyzer;

public class InvoiceAnalyzerServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = LoggerFactory.getLogger(InvoiceAnalyzerServlet.class);
	
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		OData odata = OData.newInstance();
		ServiceMetadata edm = odata.createServiceMetadata(new InvoiceAnalyzerEdmProvider(), new ArrayList<EdmxReference>());

		
		LOG.info("Got request");
		try {
			// create odata handler and configure it with EdmProvider and
			// Processor
			ODataHttpHandler handler = odata.createHandler(edm);
			
			handler.register(new InvoiceAnalyzerProcessor(Arrays.asList(new DBAnalyzer(), new ZVVAnalyzer())));

			// let the handler do the work
			handler.process(req, resp);
		} catch (Throwable e) {
			LOG.error("Server Error occurred in ExampleServlet", e);
			throw new ServletException(e);
		}

	}
}
