package odata.invoice.analyser.model;

public class AnalysisResult {

	private final double invoiceSum;

	private final String countryCode;

	private final String currencyCode;

	private final InvoiceType invoiceType;

	public AnalysisResult(double invoiceSum, String countryCode, String currencyCode, InvoiceType invoiceType) {
		super();
		this.invoiceSum = invoiceSum;
		this.countryCode = countryCode;
		this.currencyCode = currencyCode;
		this.invoiceType = invoiceType;
	}

	public double getInvoiceSum() {
		return invoiceSum;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public InvoiceType getInvoiceType() {
		return invoiceType;
	}

	public static enum InvoiceType {
		TAXI("Taxi"), 
		FLIGHT("Flüge"), 
		TRAIN_AND_OTHER_PUBLIC_TRANSPORTS("Bahn/Nahverkehr"), 
		FUEL("Tankbelege"), 
		HOTEL("Hotel"), 
		PARKING("Parkgebühr"), 
		FOOD("Bewirtung"), 
		PRIVATE_ACCOMMODATION("Übernachtung privat"), 
		OTHER("Sonstiges");

		private final String excelOutput;

		private InvoiceType(String excelOutput) {
			this.excelOutput = excelOutput;
		}

		public String getExcelOutput() {
			return excelOutput;
		}
	}

	@Override
	public String toString() {
		return String.format("AnalysisResult [invoiceSum=%s, countryCode=%s, currencyCode=%s, invoiceType=%s]",
				invoiceSum, countryCode, currencyCode, invoiceType);
	}
	
	

}
