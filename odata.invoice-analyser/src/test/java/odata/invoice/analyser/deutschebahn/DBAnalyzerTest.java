package odata.invoice.analyser.deutschebahn;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import odata.invoice.analyser.model.AnalysisResult;
import odata.invoice.analyser.model.IAnalyzer;
import odata.invoice.analyser.model.AnalysisResult.InvoiceType;

public class DBAnalyzerTest {

	IAnalyzer sut = new DBAnalyzer();

	@Test
	public void testAnalyze() {
		
		assertTrue("should be true", sut.accept("buchungsbestaetigung@bahn.de"));

		assertFalse("should be false", sut.accept("buchungsXestaetigung@bahn.de"));

		assertFalse("should be false", sut.accept("buchungsbestaetigung@Xahn.de"));
	}

	@Test
	public void testAccept() throws IOException {
		
		Path file = Paths.get("src", "test", "resources", "KBBZMB.pdf");

		byte[] binary = Files.readAllBytes(file);

		AnalysisResult analyze = sut.analyze("buchungsbestaetigung@bahn.de", binary);

		assertEquals("Should be germany", "DE", analyze.getCountryCode());
		assertEquals("Should be €, since deutsche bahn is a german company", "EUR", analyze.getCurrencyCode());

		assertEquals("is train or bus, so it should be public transport/train",
				InvoiceType.TRAIN_AND_OTHER_PUBLIC_TRANSPORTS, analyze.getInvoiceType());

		assertEquals(26.15, analyze.getInvoiceSum(), 0.0000000000001);
	}

}
