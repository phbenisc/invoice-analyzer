package odata.invoice.analyser.zvv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import odata.invoice.analyser.model.AnalysisResult;
import odata.invoice.analyser.model.AnalysisResult.InvoiceType;
import odata.invoice.analyser.model.IAnalyzer;

public class ZVVAnalyzerTest {
	
	
	
	IAnalyzer sut = new ZVVAnalyzer();

	@Test
	public void testAccept() {
		assertTrue("should be true", sut.accept("mobiletickets@zvv.ch"));
		
		assertFalse("should be false", sut.accept("mobileXickets@zvv.ch"));
		
		assertFalse("should be false", sut.accept("mobiletickets@xvv.ch"));
	}

	@Test
	public void testAnalyze() throws IOException {
		Path file = Paths.get("src", "test", "resources", "Kaufbeleg_ZVV_Mobile_Tickets_796042.pdf");
		
		byte[] binary = Files.readAllBytes(file);
		
		AnalysisResult analyze = sut.analyze("mobiletickets@zvv.ch", binary);
		
		assertEquals("Should be switzerland, since zürich is there", "CH",analyze.getCountryCode());
		assertEquals("Should be swiss franc, since ticket is from switzerland", "CHF",analyze.getCurrencyCode());
	
		assertEquals("is ttram or bus, so it should be public transport/train", InvoiceType.TRAIN_AND_OTHER_PUBLIC_TRANSPORTS,analyze.getInvoiceType());
			
		assertEquals(4.40, analyze.getInvoiceSum(), 0.0000000000001);
		
	}

}
