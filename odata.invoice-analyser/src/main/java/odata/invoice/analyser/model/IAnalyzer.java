package odata.invoice.analyser.model;

public interface IAnalyzer {
	
	boolean accept(String origin);
	
	AnalysisResult analyze(String origin, byte[] invoiceAsBinary);

}
