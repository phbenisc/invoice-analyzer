package odata.invoice.analyser.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Parameter;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmReturnType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.ActionComplexProcessor;
import org.apache.olingo.server.api.processor.ActionPrimitiveProcessor;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import odata.invoice.analyser.model.AnalysisResult;
import odata.invoice.analyser.model.IAnalyzer;

public class InvoiceAnalyzerProcessor implements ActionPrimitiveProcessor, ActionComplexProcessor {

	private final static Logger LOGGER = LoggerFactory.getLogger(InvoiceAnalyzerProcessor.class);

	private OData odata;

	private ServiceMetadata serviceMetadata;

	private List<IAnalyzer> analyzers;

	public InvoiceAnalyzerProcessor(List<IAnalyzer> analyzers) {
		this.analyzers = analyzers;
	}

	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;

	}

	@Override
	public void processActionPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType requestFormat, ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {

		process(request, response, uriInfo, requestFormat);

	}

	private void process(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat)
			throws ODataApplicationException, DeserializerException, SerializerException {
		// 1st Get the action from the resource path
		final EdmAction edmAction = ((UriResourceAction) uriInfo.asUriInfoResource().getUriResourceParts().get(0))
				.getAction();

		LOGGER.info("Parameters:", edmAction.getParameterNames());

		// 2nd Deserialize the parameter
		// In our case there is only one action. So we can be sure that
		// parameter "Amount" has been provided by the client
		if (requestFormat == null) {
			throw new ODataApplicationException("The content type has not been set in the request.",
					HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
		}

		final ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
		final Map<String, Parameter> actionParameter = deserializer.actionParameters(request.getBody(), edmAction)
				.getActionParameters();

		String origin = (String) actionParameter.get(InvoiceAnalyzerEdmProvider.ORIGIN).asPrimitive();

		byte[] invoice = (byte[]) actionParameter.get(InvoiceAnalyzerEdmProvider.INVOICE).asPrimitive();

		AnalysisResult analyze = analyze(origin, invoice);

		EdmReturnType returnType = edmAction.getReturnType();

		Property result = createAnalysisResultProperty(analyze, returnType);

		ODataSerializer serializer = odata.createSerializer(ContentType.APPLICATION_JSON);

		ContextURL contextUrl = ContextURL.with().type(edmAction).build();

		ComplexSerializerOptions options = ComplexSerializerOptions.with().contextURL(contextUrl).build();
		SerializerResult primitive = serializer.complex(serviceMetadata, (EdmComplexType) returnType.getType(), result,
				options);
		response.setStatusCode(200);
		response.setContent(primitive.getContent());
	}

	private Property createAnalysisResultProperty(AnalysisResult analyRes, EdmReturnType returnType) {

		// List<Property> returnProperties = new ArrayList<>();

		ComplexValue complexValue = new ComplexValue();

		List<Property> returnProperties = complexValue.getValue();

		returnProperties.add(createPrimitive(InvoiceAnalyzerEdmProvider.COUNTRY_CODE, analyRes.getCountryCode()));
		returnProperties.add(createPrimitive(InvoiceAnalyzerEdmProvider.CURRENCY_CODE, analyRes.getCurrencyCode()));
		returnProperties.add(createPrimitive(InvoiceAnalyzerEdmProvider.INVOICE_SUM, analyRes.getInvoiceSum()));
		returnProperties.add(
				createPrimitive(InvoiceAnalyzerEdmProvider.INVOICE_TYPE, analyRes.getInvoiceType().getExcelOutput()));

		Property property = new Property();
		// property.setType(returnType.getType().getName());
		property.setValue(ValueType.COMPLEX, complexValue);

		return property;
	}

	private Property createPrimitive(final String name, final Object value) {
		return new Property(null, name, ValueType.PRIMITIVE, value);
	}

	private AnalysisResult analyze(String origin, byte[] invoice) throws ODataApplicationException {

		for (IAnalyzer analyzer : analyzers) {
			if (analyzer.accept(origin)) {
				LOGGER.info("Analyzer {} will handle the analysis", analyzer);
				try {
					AnalysisResult analyze = analyzer.analyze(origin, invoice);
					
					LOGGER.info("analazing was successful: {}",analyze);
					
					return analyze;
				} catch (RuntimeException e) {
					throw new ODataApplicationException("Analysis failed due to an Exception", 500, Locale.ENGLISH, e);
				}
			}
		}

		throw new ODataApplicationException("origin not supported", 500, Locale.ENGLISH);
	}

	@Override
	public void processActionComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo,
			ContentType requestFormat, ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {

		try {
			process(request, response, uriInfo, requestFormat);
		} catch (Throwable e) {
			LOGGER.error("something went terrible wrong", e);
			throw e;
		}
	}

}
