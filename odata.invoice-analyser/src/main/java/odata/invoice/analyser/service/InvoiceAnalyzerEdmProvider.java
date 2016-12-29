package odata.invoice.analyser.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

public class InvoiceAnalyzerEdmProvider extends CsdlAbstractEdmProvider {
	
	public static final String NAMESPACE = "OData.Demo";

	public static final String INVOICE_TYPE = "InvoiceType";
	public static final String INVOICE_SUM = "InvoiceSum";
	public static final String CURRENCY_CODE = "CurrencyCode";
	public static final String COUNTRY_CODE = "CountryCode";
	public static final String INVOICE = "Invoice";
	public static final String ORIGIN = "Origin";

	private static final String ANALYSIS_RESULT = "AnalysisResult";
	private static final FullQualifiedName ANALYSIS_RESULT_FQN = new FullQualifiedName(NAMESPACE, ANALYSIS_RESULT);


	public static final String ACTION_ANALYZE_INVOICE = "AnalyzeInvoice";
	private static final String CONTAINER_NAME = "Container";
	private static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);


	public static final FullQualifiedName ACTION_ANALYZE_INVOICE_FQN = new FullQualifiedName(NAMESPACE,
			ACTION_ANALYZE_INVOICE);

	@Override
	public List<CsdlAction> getActions(FullQualifiedName actionName) {

		if (actionName.equals(ACTION_ANALYZE_INVOICE_FQN)) {
			// It is allowed to overload functions, so we have to provide a list
			// of functions for each function name
			final List<CsdlAction> actions = new ArrayList<>();

			// Create the parameter for the function
			final CsdlParameter origin = new CsdlParameter();
			origin.setName(ORIGIN);
			origin.setNullable(false);
			origin.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			final CsdlParameter invoice = new CsdlParameter();
			invoice.setName(INVOICE);
			invoice.setNullable(false);
			invoice.setType(EdmPrimitiveTypeKind.Binary.getFullQualifiedName());

			// Create the return type of the function
			final CsdlReturnType returnType = new CsdlReturnType();
			returnType.setCollection(false);
			returnType.setType(ANALYSIS_RESULT_FQN);

			CsdlAction action = new CsdlAction();
			action.setName(ACTION_ANALYZE_INVOICE_FQN.getName()).setParameters(Arrays.asList(origin, invoice))
					.setReturnType(returnType);

			actions.add(action);
			return actions;
		}
		return null;
	}

	@Override
	public CsdlComplexType getComplexType(FullQualifiedName complexTypeName) {

		switch (complexTypeName.getName()) {
		case ANALYSIS_RESULT:
			CsdlProperty CountryCode = new CsdlProperty().setName(COUNTRY_CODE)
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty CurrencyCode = new CsdlProperty().setName(CURRENCY_CODE)
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty InvoiceSum = new CsdlProperty().setName(INVOICE_SUM)
					.setType(EdmPrimitiveTypeKind.Double.getFullQualifiedName());

			CsdlProperty InvoiceType = new CsdlProperty().setName(INVOICE_TYPE)
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			return new CsdlComplexType().setName(ANALYSIS_RESULT)
					.setProperties(Arrays.asList(CountryCode, CurrencyCode, InvoiceSum, InvoiceType));
		default:
			return null;
		}
	}
	
	@Override
	public CsdlActionImport getActionImport(FullQualifiedName entityContainer, String actionImportName) {

		if (entityContainer.equals(CONTAINER)) {
			if (actionImportName.equals(ACTION_ANALYZE_INVOICE_FQN.getName())) {
				return new CsdlActionImport().setName(actionImportName).setAction(ACTION_ANALYZE_INVOICE_FQN);
			}
		}

		return null;
	}
	
	@Override
	public List<CsdlSchema> getSchemas() {

		// create Schema
		CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(NAMESPACE);

		// add EntityTypes
		// List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		// entityTypes.add(getEntityType(ET_PRODUCT_FQN));
		// entityTypes.add(getEntityType(ET_CATEGORY_FQN));
		// schema.setEntityTypes(entityTypes);

		List<CsdlComplexType> a = Arrays.asList(getComplexType(ANALYSIS_RESULT_FQN));
		schema.setComplexTypes(a);


		List<CsdlAction> actions = new ArrayList<>();
		actions.addAll(getActions(ACTION_ANALYZE_INVOICE_FQN));
		schema.setActions(actions);

		// add EntityContainer
		schema.setEntityContainer(getEntityContainer());

		// finally
		List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
		schemas.add(schema);

		return schemas;
	}
	
	@Override
	public CsdlEntityContainer getEntityContainer() {
		CsdlEntityContainer entityContainer = new CsdlEntityContainer();
		entityContainer.setName(CONTAINER_NAME);
		// entityContainer.setEntitySets(entitySets);
		entityContainer.setActionImports(Arrays.asList(getActionImport(CONTAINER, ACTION_ANALYZE_INVOICE_FQN.getName())));
		
		return entityContainer;

	}
	
	@Override
	public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {

		// This method is invoked when displaying the service document at
		// e.g. http://localhost:8080/DemoService/DemoService.svc
		if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
			CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
			entityContainerInfo.setContainerName(CONTAINER);
			return entityContainerInfo;
		}
		return null;
	}

}
