package odata.invoice.analyser.deutschebahn;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odata.invoice.analyser.model.AnalysisResult;
import odata.invoice.analyser.model.AnalysisResult.InvoiceType;
import odata.invoice.analyser.model.IAnalyzer;

public class DBAnalyzer implements IAnalyzer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DBAnalyzer.class);
	
	private static String regex= "Summe\\s*(\\d{1,},\\d\\d)€";

	public AnalysisResult analyze(String origin, byte[] invoiceAsBinary) {
		InvoiceType invoiceType = InvoiceType.TRAIN_AND_OTHER_PUBLIC_TRANSPORTS;
		String currencyCode = "EUR";
		String countryCode = "DE";
		
		try {
			PDFTextStripper stripper = new PDFTextStripper();
			PDDocument pdf = PDDocument.load(invoiceAsBinary);
			
			String text = stripper.getText(pdf);
			
			LOGGER.debug(text);
			
			Matcher matcher = Pattern.compile(regex).matcher(text);
			
			matcher.find();
			
			String invoiceSumString = matcher.group(1);
			
			double invoiceSum = Double.valueOf(invoiceSumString.replaceAll(",", "."));
			return new AnalysisResult(invoiceSum, countryCode, currencyCode, invoiceType);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean accept(String origin) {
		return "buchungsbestaetigung@bahn.de".equalsIgnoreCase(origin);
	}

}
