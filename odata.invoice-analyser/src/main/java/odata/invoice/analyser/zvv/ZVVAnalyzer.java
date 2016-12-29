package odata.invoice.analyser.zvv;

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

public class ZVVAnalyzer implements IAnalyzer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZVVAnalyzer.class);
	private static String regex = "Total\\s+(\\d{1,}\\.\\d\\d)";

	public boolean accept(String origin) {
		return "mobiletickets@zvv.ch".equalsIgnoreCase(origin);
	}

	public AnalysisResult analyze(String origin, byte[] invoiceAsBinary) {

		InvoiceType invoiceType = InvoiceType.TRAIN_AND_OTHER_PUBLIC_TRANSPORTS;
		String currencyCode = "CHF";
		String countryCode = "CH";

		try {
			PDFTextStripper stripper = new PDFTextStripper();
			PDDocument pdf = PDDocument.load(invoiceAsBinary);

			String text = stripper.getText(pdf);

			LOGGER.info(text);
			
			Matcher matcher = Pattern.compile(regex).matcher(text);			
			
			LOGGER.info("match found={}",matcher.find());

			String invoiceSumString = matcher.group(1);

			double invoiceSum = Double.valueOf(invoiceSumString);
			return new AnalysisResult(invoiceSum, countryCode, currencyCode, invoiceType);

		} catch (IOException e) {
			LOGGER.warn("IOException while analyzing a ZVV binary", e);
			throw new RuntimeException("could not load binary:",e);
		}
	}

}
