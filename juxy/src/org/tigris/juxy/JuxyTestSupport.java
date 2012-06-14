package org.tigris.juxy;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.URIResolver;

import junit.framework.AssertionFailedError;

import org.tigris.juxy.util.ArgumentAssert;
import org.tigris.juxy.util.DOMUtil;
import org.tigris.juxy.util.DocumentsAssertionError;
import org.tigris.juxy.util.StringUtil;
import org.tigris.juxy.util.XMLComparator;
import org.tigris.juxy.validator.ValidationFailedException;
import org.tigris.juxy.validator.ValidatorFactory;
import org.tigris.juxy.xpath.XPathAssert;
import org.tigris.juxy.xpath.XPathExpr;
import org.tigris.juxy.xpath.XPathExpressionException;
import org.tigris.juxy.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Methods for writing JUnit 4 tests for XSL templates.
 */
public class JuxyTestSupport {
	private Runner runner;
	private RunnerContext context;

	/**
	 * Creates a new RunnerContext object. RunnerContext holds all information required for calling / applying templates.
	 * 
	 * @param systemId system id of the stylesheet (path to a stylesheet file)
	 * @return new RunnerContext object
	 */
	public RunnerContext setStylesheet(String systemId) {
		context = getRunner().newRunnerContext(systemId);
		return context;
	}

	/**
	 * Creates a new RunnerContext object. RunnerContext holds all information required for calling / applying templates.
	 * 
	 * @param systemId system id of the stylesheet
	 * @param resolver URIResolver to use for URI resolution during transformation
	 * @return new RunnerContext object
	 */
	public RunnerContext setStylesheet(String systemId, URIResolver resolver) {
		context = getRunner().newRunnerContext(systemId, resolver);
		return context;
	}

	/**
	 * Sets the input document for the transformation.
	 */
	public void setDocument(Element element) throws Exception {
		setDocument(element, false);
	}

	/**
	 * Sets the input document for the transformation.
	 * @param element Document.
	 * @param printDocument If true, the document is printed to System.out.
	 */
	public void setDocument(Element element, boolean printDocument) throws Exception {
		String str = serialize(element);
		if (printDocument) {
			System.out.println(str);
		}
		getContext().setDocument(str);
	}

	/**
	 * Creates new XPathExpr object.
	 * 
	 * @param xpathExpr an XPath expression
	 * @return new XPathExpr object
	 * @throws Exception
	 */
	public XPathExpr xpath(String xpathExpr) throws Exception {
		return XPathFactory.newXPath(xpathExpr);
	}

	/**
	 * See {@link Runner#applyTemplates(RunnerContext)}
	 */
	public Node applyTemplates() throws Exception {
		return getRunner().applyTemplates(getContext());
	}

	/**
	 * See {@link Runner#applyTemplates(RunnerContext, org.tigris.juxy.xpath.XPathExpr)}
	 */
	public Node applyTemplates(XPathExpr xpath) throws Exception {
		return getRunner().applyTemplates(getContext(), xpath);
	}

	/**
	 * See {@link Runner#applyTemplates(RunnerContext, org.tigris.juxy.xpath.XPathExpr, String)}
	 */
	public Node applyTemplates(XPathExpr xpath, String mode) throws Exception {
		return getRunner().applyTemplates(getContext(), xpath, mode);
	}

	/**
	 * See {@link Runner#callTemplate(RunnerContext, String)}
	 */
	public Node callTemplate(String name) throws Exception {
		return getRunner().callTemplate(getContext(), name);
	}

	/**
	 * Asserts two documents are equal. Meaningless spaces will be ignored during this assertion.
	 * 
	 * @param expected XML document which is expected
	 * @param actual document root node of actual transformation result
	 * @throws Exception
	 */
	public static void assertXMLEquals(Node expected, Node actual) throws Exception {
		try {
			XMLComparator.assertEquals(expected, actual);
		} catch (DocumentsAssertionError error) {
			throw new AssertionFailedError(error.getMessage());
		}
	}

	/**
	 * Asserts two documents are equal. Meaningless spaces will be ignored during this assertion.
	 * 
	 * @param expectedDocument XML document which is expected
	 * @param actual document root node of actual transformation result
	 * @throws Exception
	 */
	public static void assertXMLEquals(String expectedDocument, Node actual) throws Exception {
		try {
			XMLComparator.assertEquals(expectedDocument, actual);
		} catch (DocumentsAssertionError error) {
			throw new AssertionFailedError(error.getMessage());
		}
	}

	/**
	 * Asserts two documents are equal. Meaningless spaces will be ignored during this assertion.
	 * 
	 * @param expectedDocument XML document which is expected
	 * @param actualDocument actual xml document
	 * @throws Exception
	 */
	public static void assertXMLEquals(String expectedDocument, String actualDocument) throws Exception {
		try {
			XMLComparator.assertEquals(expectedDocument, actualDocument);
		} catch (DocumentsAssertionError error) {
			throw new AssertionFailedError(error.getMessage());
		}
	}

	/**
	 * Converts the node to a string and asserts that the result is equal to the expected result.
	 * @param expected Expected result.
	 * @param actual Node to compare.
	 */
	public void assertStringEquals(String expected, org.w3c.dom.Node actual) throws Exception {
		xpathAssert(".", expected).eval(actual);
	}

	/**
	 * See {@link StringUtil#normalizeSpaces(String)}
	 * 
	 * @param str string to normalize
	 * @return normalized string
	 */
	public String normalizeSpaces(String str) {
		return StringUtil.normalizeSpaces(str);
	}

	/**
	 * See {@link StringUtil#normalizeAll(String)}
	 * 
	 * @param str string to normalize
	 * @return normalized string
	 */
	public String normalizeAll(String str) {
		return StringUtil.normalizeAll(str);
	}

	/**
	 * Prints fragment of the document to System.out starting from the specified node.
	 * 
	 * @param node node to display
	 * @throws Exception
	 */
	public void print(Node node) throws Exception {
		ArgumentAssert.notNull(node, "Node must not be null");
		System.out.println(serialize(node));
	}

	/**
	 * Serializes fragment of the document to String, starting from the specified node.
	 * 
	 * @param node node to display
	 * @return xml document corresponding to the specified node
	 * @throws Exception
	 */
	public String serialize(Node node) throws Exception {
		ArgumentAssert.notNull(node, "Node must not be null");
		ByteArrayOutputStream bos = new ByteArrayOutputStream(100);
		DOMUtil.printDOM(node, bos);
		return bos.toString();
	}

	/**
	 * Parses specified string into org.w3c.dom.Document.
	 * 
	 * @param document xml document
	 * @return DOM Document
	 * @throws Exception
	 */
	public Document parse(String document) throws Exception {
		ArgumentAssert.notEmpty(document, "Document must not be empty");
		return DOMUtil.parse(document);
	}

	/**
	 * See {@link Runner#enableTracing()}.
	 */
	public void enableTracing() {
		getRunner().enableTracing();
	}

	/**
	 * See {@link Runner#disableTracing()}.
	 */
	public void disableTracing() {
		getRunner().disableTracing();
	}

	/**
	 * Validates specified node using XML schema with specified path.
	 * 
	 * @param node node to validate
	 * @param pathToSchema path to W3C XML schema
	 */
	public void validateWithSchema(Node node, String pathToSchema) {
		try {
			ValidatorFactory.createXMLSchemaValidator(pathToSchema).validate(node);
		} catch (ValidationFailedException e) {
			throw new AssertionFailedError(e.getMessage());
		}
	}

	/**
	 * Evaluates XPath assertions.
	 * 
	 * @param node node to evaluate assertions on
	 * @param assertions XPath assertions to evaluate
	 * @throws XPathExpressionException
	 */
	public void evalAssertions(Node node, XPathAssert[] assertions) throws XPathExpressionException {
		ArgumentAssert.notNull(assertions, "Assertions must not be null");
		try {
			for (int i = 0; i < assertions.length; i++) {
				assertions[i].eval(node);
			}
		} catch (AssertionError error) {
			throw new AssertionFailedError(error.getMessage());
		}
	}

	/**
	 * See {@link XPathAssert#XPathAssert(String, int)}
	 */
	public XPathAssert xpathAssert(String xpathExpr, int expectedResult) {
		return new XPathAssert(xpathExpr, expectedResult) {
			public void eval(Node node) throws XPathExpressionException {
				try {
					super.eval(node);
				} catch (AssertionError error) {
					throw new AssertionFailedError(error.getMessage());
				}
			}
		};
	}

	/**
	 * See {@link XPathAssert#XPathAssert(String, boolean)}
	 */
	public XPathAssert xpathAssert(String xpathExpr, boolean expectedResult) {
		return new XPathAssert(xpathExpr, expectedResult) {
			public void eval(Node node) throws XPathExpressionException {
				try {
					super.eval(node);
				} catch (AssertionError error) {
					throw new AssertionFailedError(error.getMessage());
				}
			}
		};
	}

	/**
	 * See {@link XPathAssert#XPathAssert(String, String)}
	 */
	public XPathAssert xpathAssert(String xpathExpr, String expectedResult) {
		return new XPathAssert(xpathExpr, expectedResult) {
			public void eval(Node node) throws XPathExpressionException {
				try {
					super.eval(node);
				} catch (AssertionError error) {
					throw new AssertionFailedError(error.getMessage());
				}
			}
		};
	}

	/**
	 * See {@link XPathAssert#XPathAssert(String, String, boolean)}
	 */
	public XPathAssert xpathAssert(String xpathExpr, String expectedResult, boolean normalize) {
		return new XPathAssert(xpathExpr, expectedResult, normalize) {
			public void eval(Node node) throws XPathExpressionException {
				try {
					super.eval(node);
				} catch (AssertionError error) {
					throw new AssertionFailedError(error.getMessage());
				}
			}
		};
	}

	/**
	 * See {@link org.tigris.juxy.xpath.XPathAssert#XPathAssert(String, double, double)}
	 */
	public XPathAssert xpathAssert(String xpathExpr, double expectedResult, double error) {
		return new XPathAssert(xpathExpr, expectedResult, error) {
			public void eval(Node node) throws XPathExpressionException {
				try {
					super.eval(node);
				} catch (AssertionError error) {
					throw new AssertionFailedError(error.getMessage());
				}
			}
		};
	}

	public RunnerContext getContext() {
		if (context == null)
			throw new IllegalStateException("Call setStylesheet method first");
		return context;
	}

	private Runner getRunner() {
		if (runner == null)
			runner = RunnerFactory.newRunner();
		return runner;
	}
}
